"""Herramienta MCP: buscar_producto.

Realiza búsqueda semántica de productos mediante el AI Service.
"""

from typing import Any, Dict, List

from mcp.server.fastmcp import FastMCP

from app.clients.ai_client import AIClient
from app.config import settings

ai_client = AIClient(settings.ai_service_url, settings.internal_api_key)


def register(mcp: FastMCP) -> None:
    @mcp.tool(
        name="buscar_producto",
        description=(
            "Busca productos del inventario utilizando búsqueda semántica "
            "(embeddings vectoriales). Útil cuando el usuario describe un producto "
            "con sus propias palabras, sin conocer el SKU o nombre exacto."
        ),
    )
    async def buscar_producto(query: str, limit: int = 5) -> str:
        """Busca productos semánticamente por consulta en lenguaje natural.

        Args:
            query: Consulta del usuario (ej. "laptop liviana para programar").
            limit: Número máximo de resultados (1-50). Por defecto 5.

        Returns:
            Texto formateado con los productos más relevantes.
        """
        if not query or not query.strip():
            return "La consulta de búsqueda no puede estar vacía."

        try:
            results: List[Dict[str, Any]] = await ai_client.semantic_search(
                query=query.strip(), limit=max(1, min(int(limit), 50))
            )
        except Exception as exc:  # noqa: BLE001 - se devuelve mensaje al agente
            return f"Error al consultar búsqueda semántica: {exc}"

        if not results:
            return f"No se encontraron productos que coincidan con: '{query}'"

        lines = [f"Resultados de búsqueda semántica para '{query}':"]
        for idx, item in enumerate(results, start=1):
            product_id = item.get("productId", "N/A")
            score = item.get("score", 0)
            rationale = item.get("rationale", "Coincidencia semántica.")
            lines.append(
                f"{idx}. [{product_id}] similitud {score:.0%} — {rationale}"
            )

        return "\n".join(lines)
