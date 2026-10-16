"""Herramienta MCP: consultar_documentacion.

Ejecuta consultas RAG sobre documentación técnica de productos mediante el AI Service.
"""

from typing import Any, Dict, Optional

from mcp.server.fastmcp import FastMCP

from app.clients.ai_client import AIClient
from app.config import settings

ai_client = AIClient(settings.ai_service_url, settings.internal_api_key)


def register(mcp: FastMCP) -> None:
    @mcp.tool(
        name="consultar_documentacion",
        description=(
            "Consulta documentación técnica de productos mediante RAG (Retrieval-Augmented "
            "Generation). Recupera fragmentos relevantes y genera una respuesta "
            "contextualizada usando el modelo LLM local."
        ),
    )
    async def consultar_documentacion(
        question: str,
        product_id: Optional[str] = None,
    ) -> str:
        """Responde una pregunta sobre documentación técnica de productos.

        Args:
            question: Pregunta en lenguaje natural (ej. "¿Cómo realizar mantenimiento
                     preventivo de este equipo?").
            product_id: (Opcional) ID del producto para limitar el contexto a su
                        documentación específica.

        Returns:
            Respuesta generada por el LLM con las fuentes utilizadas.
        """
        if not question or not question.strip():
            return "La pregunta no puede estar vacía."

        try:
            result: Dict[str, Any] = await ai_client.rag_query(
                question=question.strip(),
                product_id=product_id,
            )
        except Exception as exc:  # noqa: BLE001
            return f"Error al consultar la documentación: {exc}"

        answer = result.get("answer", "No se pudo generar una respuesta.")
        sources = result.get("sources", [])

        lines = [answer]
        if sources:
            lines.append("")
            lines.append("Fuentes:")
            for idx, src in enumerate(sources, start=1):
                preview = src[:120] + ("…" if len(src) > 120 else "")
                lines.append(f"{idx}. {preview}")

        return "\n".join(lines)
