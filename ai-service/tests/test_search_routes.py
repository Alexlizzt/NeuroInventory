"""Pruebas de los endpoints HTTP de búsqueda semántica.

Se monta una app FastAPI mínima con el router de búsqueda y se reemplaza el
servicio por un doble, de modo que no se necesita PostgreSQL ni Ollama.
"""

import pytest
from fastapi import FastAPI
from fastapi.testclient import TestClient

from app.api.search_routes import router, get_search_service
from app.config import settings

API_KEY = "test-internal-key"


class FakeSearchService:
    def __init__(self):
        self.indexed: list[dict] = []
        self.deleted: list[str] = []
        self.search_result: list[dict] = []
        self.raise_on_index = False
        self.raise_on_search = False

    def index_product(self, product_id, name=None, sku=None, description=None, metadata=None):
        if self.raise_on_index:
            raise RuntimeError("boom")
        if not (name or sku or description):
            return False
        self.indexed.append({
            "product_id": product_id,
            "name": name,
            "sku": sku,
            "description": description,
            "metadata": metadata,
        })
        return True

    def delete_product(self, product_id):
        self.deleted.append(product_id)

    def search(self, query, limit=5):
        if self.raise_on_search:
            raise RuntimeError("boom")
        return self.search_result


@pytest.fixture
def fake_service():
    return FakeSearchService()


@pytest.fixture
def client(fake_service, monkeypatch):
    monkeypatch.setattr(settings, "internal_api_key", API_KEY)
    app = FastAPI()
    app.include_router(router, prefix="/api/v1")
    app.dependency_overrides[get_search_service] = lambda: fake_service
    with TestClient(app) as test_client:
        yield test_client


def auth_headers():
    return {"X-API-KEY": API_KEY}


# ---------------------------------------------------------------------------
# Seguridad
# ---------------------------------------------------------------------------
class TestAuth:
    def test_rejects_missing_api_key(self, client):
        response = client.post("/api/v1/search/semantic", json={"query": "hola"})
        assert response.status_code == 401

    def test_rejects_wrong_api_key(self, client):
        response = client.post(
            "/api/v1/search/semantic",
            json={"query": "hola"},
            headers={"X-API-KEY": "incorrecta"},
        )
        assert response.status_code == 403


# ---------------------------------------------------------------------------
# Indexado
# ---------------------------------------------------------------------------
class TestIndexEndpoint:
    def test_indexes_product(self, client, fake_service):
        response = client.post(
            "/api/v1/search/index",
            json={"productId": "p1", "name": "Monitor", "sku": "MON-1", "description": "4K"},
            headers=auth_headers(),
        )
        assert response.status_code == 200
        assert response.json() == {"status": "SUCCESS", "productId": "p1"}
        assert fake_service.indexed[0]["product_id"] == "p1"

    def test_returns_400_when_not_indexable(self, client):
        response = client.post(
            "/api/v1/search/index",
            json={"productId": "p1"},
            headers=auth_headers(),
        )
        assert response.status_code == 400

    def test_returns_500_on_service_error(self, client, fake_service):
        fake_service.raise_on_index = True
        response = client.post(
            "/api/v1/search/index",
            json={"productId": "p1", "name": "Monitor"},
            headers=auth_headers(),
        )
        assert response.status_code == 500

    def test_delete_index_returns_204(self, client, fake_service):
        response = client.delete("/api/v1/search/index/p1", headers=auth_headers())
        assert response.status_code == 204
        assert fake_service.deleted == ["p1"]


# ---------------------------------------------------------------------------
# Búsqueda
# ---------------------------------------------------------------------------
class TestSemanticEndpoint:
    def test_returns_matches_in_backend_contract_format(self, client, fake_service):
        fake_service.search_result = [
            {"productId": "p1", "score": 0.9, "rationale": "Match"},
        ]
        response = client.post(
            "/api/v1/search/semantic",
            json={"query": "monitor", "limit": 3},
            headers=auth_headers(),
        )
        assert response.status_code == 200
        body = response.json()
        # El backend espera camelCase: matches[].productId y interpretedQuery
        assert body["matches"][0]["productId"] == "p1"
        assert body["matches"][0]["score"] == 0.9
        assert body["interpretedQuery"] == "monitor"

    def test_returns_empty_matches(self, client, fake_service):
        fake_service.search_result = []
        response = client.post(
            "/api/v1/search/semantic",
            json={"query": "nada"},
            headers=auth_headers(),
        )
        assert response.status_code == 200
        assert response.json()["matches"] == []

    def test_validates_limit_bounds(self, client):
        response = client.post(
            "/api/v1/search/semantic",
            json={"query": "monitor", "limit": 0},
            headers=auth_headers(),
        )
        assert response.status_code == 422

    def test_returns_500_on_service_error(self, client, fake_service):
        fake_service.raise_on_search = True
        response = client.post(
            "/api/v1/search/semantic",
            json={"query": "monitor"},
            headers=auth_headers(),
        )
        assert response.status_code == 500
