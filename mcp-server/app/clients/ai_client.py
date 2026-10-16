"""Cliente para comunicarse con el AI Service de NeuroInventory.

Utiliza la cabecera interna `X-API-KEY` (`INTERNAL_API_KEY`) para autenticación,
tal como está definido en la arquitectura de seguridad del proyecto.
"""

from typing import Any, Dict, List, Optional

import httpx


class AIClient:
    def __init__(self, base_url: str, internal_api_key: str) -> None:
        self.base_url = base_url.rstrip("/")
        self.api_key = internal_api_key

    def _headers(self) -> Dict[str, str]:
        return {
            "X-API-KEY": self.api_key,
            "Content-Type": "application/json",
        }

    async def semantic_search(
        self, query: str, limit: int = 5
    ) -> List[Dict[str, Any]]:
        """Ejecuta búsqueda semántica de productos en el AI Service.

        Endpoint: POST /api/v1/search/semantic
        """
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(
                f"{self.base_url}/api/v1/search/semantic",
                json={"query": query, "limit": limit},
                headers=self._headers(),
            )
            response.raise_for_status()
            data = response.json()
            return data.get("matches", [])

    async def rag_query(
        self, question: str, product_id: Optional[str] = None
    ) -> Dict[str, Any]:
        """Ejecuta consulta RAG (documentación) en el AI Service.

        Endpoint: POST /api/v1/rag/query
        """
        payload: Dict[str, Any] = {"question": question}
        if product_id:
            payload["product_id"] = product_id

        async with httpx.AsyncClient(timeout=60.0) as client:
            response = await client.post(
                f"{self.base_url}/api/v1/rag/query",
                json=payload,
                headers=self._headers(),
            )
            response.raise_for_status()
            return response.json()
