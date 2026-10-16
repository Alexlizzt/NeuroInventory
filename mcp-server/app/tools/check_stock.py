"""Herramienta MCP: consultar_stock.

Consulta la disponibilidad de stock de un producto mediante el Inventory Service.
"""

from typing import Any, Dict, Optional

from mcp.server.fastmcp import FastMCP

from app.clients.inventory_client import InventoryClient
from app.config import settings

inventory_client = InventoryClient(
    settings.inventory_service_url, settings.service_token
)


def register(mcp: FastMCP) -> None:
    @mcp.tool(
        name="consultar_stock",
        description=(
            "Consulta el stock actual y el nivel mínimo de un producto en el inventario. "
            "Requiere el identificador del producto (product_id)."
        ),
    )
    async def consultar_stock(
        product_id: str,
        access_token: Optional[str] = None,
    ) -> str:
        """Obtiene la disponibilidad de stock de un producto.

        Args:
            product_id: Identificador único del producto.
            access_token: (Opcional) Token JWT del usuario autenticado. Si no se provee,
                          se usa SERVICE_TOKEN del servidor MCP si está configurado.

        Returns:
            Texto con el stock disponible y si está por debajo del mínimo.
        """
        if not product_id:
            return "El identificador del producto es obligatorio."

        try:
            data: Dict[str, Any] = await inventory_client.get_stock(
                product_id=product_id, access_token=access_token
            )
        except Exception as exc:  # noqa: BLE001
            return f"Error al consultar el stock del producto {product_id}: {exc}"

        quantity = data.get("quantity", "N/D")
        min_stock = data.get("minStock", "N/D")
        is_low_stock = data.get("isLowStock", False)

        estado = "⚠️ STOCK BAJO" if is_low_stock else "✅ Disponible"

        return (
            f"Producto: {product_id}\n"
            f"Stock actual: {quantity}\n"
            f"Stock mínimo: {min_stock}\n"
            f"Estado: {estado}"
        )
