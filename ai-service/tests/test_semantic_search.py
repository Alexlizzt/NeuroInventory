"""Pruebas unitarias del servicio de búsqueda semántica.

No requieren PostgreSQL ni Ollama: se inyectan dobles (fake engine y fake
embedding service) para aislar la lógica de construcción de SQL y mapeo.
"""

from app.search.semantic import SemanticSearchService


# ---------------------------------------------------------------------------
# Dobles de prueba
# ---------------------------------------------------------------------------
class FakeEmbeddingService:
    def __init__(self, vector=None):
        self.vector = vector or [0.1] * 768
        self.calls: list[str] = []

    def generate_embedding(self, text: str):
        self.calls.append(text)
        return self.vector


class FakeResult:
    def __init__(self, rows=None, scalar=None):
        self._rows = rows if rows is not None else []
        self._scalar = scalar

    def mappings(self):
        return self

    def scalars(self):
        return self

    def all(self):
        return self._rows

    def scalar(self):
        return self._scalar


class FakeConnection:
    def __init__(self, engine):
        self.engine = engine

    def execute(self, statement, params=None):
        sql = " ".join(str(statement).split())
        self.engine.executed.append((sql, params))

        if "format_type" in sql:
            return FakeResult(scalar=self.engine.column_type)
        if "COUNT(*)" in sql:
            return FakeResult(scalar=self.engine.row_count)
        if "SELECT product_id" in sql:
            return FakeResult(rows=self.engine.search_rows)
        if "pg_constraint" in sql:
            return FakeResult(rows=self.engine.foreign_keys)
        return FakeResult()

    def __enter__(self):
        return self

    def __exit__(self, *_):
        return False


class FakeEngine:
    def __init__(self, column_type="vector(768)", row_count=0, search_rows=None, foreign_keys=None):
        self.column_type = column_type
        self.row_count = row_count
        self.search_rows = search_rows or []
        self.foreign_keys = foreign_keys or []
        self.executed: list[tuple[str, dict | None]] = []

    def begin(self):
        return FakeConnection(self)

    def connect(self):
        return FakeConnection(self)

    def executed_sql(self):
        return [sql for sql, _ in self.executed]


def build_service(engine=None, embedding=None):
    engine = engine or FakeEngine()
    embedding = embedding or FakeEmbeddingService()
    return SemanticSearchService(db_engine=engine, embedding_service=embedding), engine, embedding


# ---------------------------------------------------------------------------
# Helpers puros
# ---------------------------------------------------------------------------
class TestHelpers:
    def test_to_pgvector_formats_float_list(self):
        assert SemanticSearchService._to_pgvector([1.0, 2.5, -3.0]) == "[1.0,2.5,-3.0]"

    def test_build_search_text_joins_present_parts(self):
        text = SemanticSearchService._build_search_text("  Monitor  ", "MON-1", " 4K ")
        assert text == "Monitor MON-1 4K"

    def test_build_search_text_skips_empty_parts(self):
        assert SemanticSearchService._build_search_text("Monitor", None, "   ") == "Monitor"

    def test_build_rationale_includes_name_and_score(self):
        rationale = SemanticSearchService._build_rationale(0.9, {"name": "Mouse"})
        assert "90%" in rationale
        assert "Mouse" in rationale

    def test_build_rationale_without_name(self):
        rationale = SemanticSearchService._build_rationale(0.5, None)
        assert "50%" in rationale


# ---------------------------------------------------------------------------
# Esquema
# ---------------------------------------------------------------------------
class TestSchema:
    def test_creates_extension_and_table_on_init(self):
        _, engine, _ = build_service()
        sql = " ".join(engine.executed_sql())
        assert "CREATE EXTENSION IF NOT EXISTS vector" in sql
        assert "CREATE TABLE IF NOT EXISTS product_embeddings" in sql

    def test_reconciles_dimension_when_table_empty(self):
        engine = FakeEngine(column_type="vector(1536)", row_count=0)
        build_service(engine=engine)
        assert any("ALTER TABLE product_embeddings" in sql for sql in engine.executed_sql())

    def test_does_not_alter_dimension_when_table_has_rows(self):
        engine = FakeEngine(column_type="vector(1536)", row_count=5)
        build_service(engine=engine)
        assert not any("ALTER TABLE product_embeddings" in sql for sql in engine.executed_sql())

    def test_drops_legacy_foreign_keys(self):
        engine = FakeEngine(foreign_keys=["fk_embedding_product"])
        build_service(engine=engine)
        assert any(
            "DROP CONSTRAINT" in sql and "fk_embedding_product" in sql
            for sql in engine.executed_sql()
        )


# ---------------------------------------------------------------------------
# Indexación
# ---------------------------------------------------------------------------
class TestIndexProduct:
    def test_returns_false_when_no_indexable_text(self):
        service, _, embedding = build_service()
        assert service.index_product("p1", None, None, "  ") is False
        assert embedding.calls == []

    def test_returns_false_when_product_id_missing(self):
        service, _, embedding = build_service()
        assert service.index_product("", "Nombre", "SKU", "Desc") is False
        assert embedding.calls == []

    def test_upserts_embedding(self):
        service, engine, embedding = build_service()
        result = service.index_product("p1", "Monitor", "MON-1", "4K", {"categoryId": "c1"})

        assert result is True
        assert embedding.calls == ["Monitor MON-1 4K"]

        insert = [sql for sql in engine.executed_sql() if "INSERT INTO product_embeddings" in sql]
        assert insert, "Debe emitir un INSERT ... ON CONFLICT"
        assert "ON CONFLICT (product_id) DO UPDATE" in insert[0]

    def test_delete_product_executes_delete(self):
        service, engine, _ = build_service()
        service.delete_product("p1")
        assert any("DELETE FROM product_embeddings" in sql for sql in engine.executed_sql())


# ---------------------------------------------------------------------------
# Búsqueda
# ---------------------------------------------------------------------------
class TestSearch:
    def test_returns_empty_on_blank_query_without_embedding(self):
        service, _, embedding = build_service()
        assert service.search("   ") == []
        assert embedding.calls == []

    def test_maps_rows_to_matches(self):
        rows = [
            {"product_id": "p1", "score": 0.91, "metadata": {"name": "Monitor 4K"}},
            {"product_id": "p2", "score": 0.55, "metadata": {"name": "Teclado"}},
        ]
        service, engine, embedding = build_service(engine=FakeEngine(search_rows=rows))

        matches = service.search("pantalla grande", limit=2)

        assert [m["productId"] for m in matches] == ["p1", "p2"]
        assert matches[0]["score"] == 0.91
        assert "Monitor 4K" in matches[0]["rationale"]
        assert embedding.calls == ["pantalla grande"]

        select = [sql for sql in engine.executed_sql() if "SELECT product_id" in sql]
        assert select, "Debe consultar la tabla de embeddings"
        assert "ORDER BY embedding <=> CAST(:embedding AS vector)" in select[0]
