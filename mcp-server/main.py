"""Punto de entrada del servidor MCP de NeuroInventory.

Expone herramientas del dominio (productos, stock, movimientos, documentación)
mediante el protocolo Model Context Protocol (MCP) sobre transporte SSE.
"""

from mcp.server.fastmcp import FastMCP

from app.config import settings
from app.tools import register_tools

# Crear servidor MCP
mcp = FastMCP("NeuroInventory MCP Server")

# Registrar todas las herramientas de dominio
register_tools(mcp)

if __name__ == "__main__":
    # Transporte SSE para accesibilidad por red (agentes externos)
    mcp.run(transport="sse", host=settings.mcp_host, port=settings.mcp_port)
