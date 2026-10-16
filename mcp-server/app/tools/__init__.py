"""Registro de herramientas MCP de NeuroInventory."""

from mcp.server.fastmcp import FastMCP

from .search_product import register as register_search_product
from .check_stock import register as register_check_stock
from .create_movement import register as register_create_movement
from .query_documentation import register as register_query_documentation


def register_tools(mcp: FastMCP) -> None:
    """Registra todas las herramientas de dominio en el servidor MCP."""
    register_search_product(mcp)
    register_check_stock(mcp)
    register_create_movement(mcp)
    register_query_documentation(mcp)
