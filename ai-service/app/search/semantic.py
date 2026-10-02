import json
import logging

from sqlalchemy import create_engine, text
from sqlalchemy.engine import Engine

from app.config import settings
from app.embeddings.generator import EmbeddingService

logger = logging.getLogger(__name__)

# Dimensión del modelo de embeddings local (nomic-embed-text => 768)
EMBEDDING_DIM = 768

TABLE_NAME = "product_embeddings"


def _build_engine() -> Engine:
    return create_engine(settings.database_url, pool_pre_ping=True)


class SemanticSearchService:
    """Indexa productos en pgvector y resuelve búsquedas por similitud semántica."""

    def __init__(self, db_engine: Engine | None = None, embedding_service: EmbeddingService | None = None):
        self.engine = db_engine or _build_engine()
        self.embedding_service = embedding_service or EmbeddingService()
        self._ensure_schema()

    # ------------------------------------------------------------------
    # Helpers
    # ------------------------------------------------------------------
    @staticmethod
    def _to_pgvector(vector: list[float]) -> str:
        """Convierte una lista de floats al literal textual que espera pgvector."""
        return "[" + ",".join(repr(float(value)) for value in vector) + "]"

    @staticmethod
    def _build_search_text(name: str | None, sku: str | None, description: str | None) -> str:
        return " ".join(part.strip() for part in (name, sku, description) if part and part.strip())

    @staticmethod
    def _build_rationale(score: float, metadata: dict | None) -> str:
        metadata = metadata or {}
        name = metadata.get("name")
        if name:
            return f"Coincidencia semántica del {score:.0%} con '{name}'."
        return f"Coincidencia semántica del {score:.0%} con la consulta."

    # ------------------------------------------------------------------
    # Esquema
    # ------------------------------------------------------------------
    def _ensure_schema(self) -> None:
        """Garantiza la extensión, la tabla y la dimensión correcta del vector."""
        try:
            with self.engine.begin() as conn:
                conn.execute(text("CREATE EXTENSION IF NOT EXISTS vector;"))
                conn.execute(text(f"""
                    CREATE TABLE IF NOT EXISTS {TABLE_NAME} (
                        product_id VARCHAR(36) PRIMARY KEY,
                        embedding vector({EMBEDDING_DIM}),
                        metadata JSONB,
                        updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
                    );
                """))
                self._drop_legacy_foreign_keys(conn)
                self._reconcile_dimension(conn)
        except Exception as exc:  # pragma: no cover - arranque defensivo
            logger.warning("No se pudo verificar el esquema de %s: %s", TABLE_NAME, exc)

    def _drop_legacy_foreign_keys(self, conn) -> None:
        """Elimina FKs heredadas hacia products para mantener el desacoplamiento entre servicios."""
        constraints = conn.execute(text("""
            SELECT con.conname
            FROM pg_constraint con
            JOIN pg_class rel ON rel.oid = con.conrelid
            WHERE rel.relname = :table AND con.contype = 'f'
        """), {"table": TABLE_NAME}).scalars().all()

        for constraint in constraints:
            conn.execute(text(
                f'ALTER TABLE {TABLE_NAME} DROP CONSTRAINT IF EXISTS "{constraint}"'
            ))
            logger.warning("FK heredada '%s' eliminada de %s", constraint, TABLE_NAME)


    def _reconcile_dimension(self, conn) -> None:
        """Corrige tablas antiguas creadas con otra dimensión si están vacías."""
        current_type = conn.execute(text("""
            SELECT format_type(a.atttypid, a.atttypmod)
            FROM pg_attribute a
            JOIN pg_class c ON c.oid = a.attrelid
            WHERE c.relname = :table AND a.attname = 'embedding' AND NOT a.attisdropped
        """), {"table": TABLE_NAME}).scalar()

        expected_type = f"vector({EMBEDDING_DIM})"
        if current_type and current_type != expected_type:
            row_count = conn.execute(text(f"SELECT COUNT(*) FROM {TABLE_NAME}")).scalar()
            if row_count == 0:
                conn.execute(text(
                    f"ALTER TABLE {TABLE_NAME} ALTER COLUMN embedding TYPE vector({EMBEDDING_DIM})"
                ))
                logger.warning(
                    "Dimensión de %s.embedding ajustada de %s a %s",
                    TABLE_NAME, current_type, expected_type,
                )
            else:
                logger.warning(
                    "La tabla %s usa %s pero se esperaba %s. Se requiere reindexar.",
                    TABLE_NAME, current_type, expected_type,
                )

    # ------------------------------------------------------------------
    # Indexación
    # ------------------------------------------------------------------
    def index_product(self, product_id: str, name: str | None, sku: str | None,
                      description: str | None, metadata: dict | None = None) -> bool:
        content = self._build_search_text(name, sku, description)
        if not product_id or not content:
            logger.warning("Producto %s sin texto indexable; se omite.", product_id)
            return False

        vector = self._to_pgvector(self.embedding_service.generate_embedding(content))
        payload = {
            "product_id": product_id,
            "embedding": vector,
            "metadata": json.dumps({**(metadata or {}), "name": name, "sku": sku}),
        }

        with self.engine.begin() as conn:
            conn.execute(text(f"""
                INSERT INTO {TABLE_NAME} (product_id, embedding, metadata, updated_at)
                VALUES (:product_id, CAST(:embedding AS vector), CAST(:metadata AS jsonb), NOW())
                ON CONFLICT (product_id) DO UPDATE
                    SET embedding = EXCLUDED.embedding,
                        metadata = EXCLUDED.metadata,
                        updated_at = NOW()
            """), payload)
        return True

    def delete_product(self, product_id: str) -> None:
        with self.engine.begin() as conn:
            conn.execute(
                text(f"DELETE FROM {TABLE_NAME} WHERE product_id = :product_id"),
                {"product_id": product_id},
            )

    # ------------------------------------------------------------------
    # Búsqueda
    # ------------------------------------------------------------------
    def search(self, query: str, limit: int = 5) -> list[dict]:
        content = (query or "").strip()
        if not content:
            return []

        vector = self._to_pgvector(self.embedding_service.generate_embedding(content))
        with self.engine.connect() as conn:
            rows = conn.execute(text(f"""
                SELECT product_id,
                       1 - (embedding <=> CAST(:embedding AS vector)) AS score,
                       metadata
                FROM {TABLE_NAME}
                WHERE embedding IS NOT NULL
                ORDER BY embedding <=> CAST(:embedding AS vector)
                LIMIT :limit
            """), {"embedding": vector, "limit": max(1, int(limit))}).mappings().all()

        return [
            {
                "productId": row["product_id"],
                "score": float(row["score"]),
                "rationale": self._build_rationale(float(row["score"]), row["metadata"]),
            }
            for row in rows
        ]
