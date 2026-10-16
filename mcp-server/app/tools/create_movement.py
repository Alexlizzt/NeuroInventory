"""Herramienta MCP: crear_movimiento.

Registra una entrada, salida o ajuste de inventario mediante el Inventory Service.
"""

from typing import Optional

from mcp.server.fastmcp import FastMCP

from app.clients.inventory_client import InventoryClient
from app.config import settings

inventory_client = InventoryClient(
    settings.inventory_service_url, settings.service_token
)


def register(mcp: FastMCP) -> None:
    @mcp.tool(
        name="crear_movimiento",
        description=(
            "Registra un movimiento de inventario (entrada IN, salida OUT o ajuste "
            "ADJUSTMENT) para un producto. Requiere autenticación válida en el "
            "Inventory Service."
        ),
    )
    async def crear_movimiento(
        product_id: str,
        movement_type: str,
        quantity: int,
        reason: str,
        access_token: Optional[str] = None,
    ) -> str:
        """Crea un movimiento de inventario.

        Args:
            product_id: Identificador del producto.
            movement_type: Tipo de movimiento (IN, OUT, ADJUSTMENT).
            quantity: Cantidad de unidades.
            reason: Motivo o descripción del movimiento.
            access_token: (Opcional) Token JWT del usuario autenticado.

        Returns:
            Confirmación del movimiento creado o mensaje de error.
        """
        valid_types = {"IN", "OUT", "ADJUSTMENT"}
        if movement_type.upper() not in valid_types:
            return (
                f"Tipo de movimiento inválido: '{movement_type}'. "
                f"Valores permitidos: {', '.join(sorted(valid_types))}."
            )

        if quantity <= 0:
            return "La cantidad debe ser un número mayor a cero."

        if not product_id or not reason:
            return "product_id y reason son obligatorios."

        try:
            result: dict = await inventory_client.create_movement(
                product_id=product_id,
                movement_type=movement_type.upper(),
                quantity=int(quantity),
                reason=reason,
                access_token=access_token,
            )
        except Exception as exc:  # noqa: BLE001
            return f"Error al registrar el movimiento: {exc}"

        movement_id = result.get("id", "N/D")
        return (
            f"Movimiento registrado exitosamente.\n"
            f"ID: {movement_id}\n"
            f"Producto: {product_id}\n"
            f"Tipo: {movement_type.upper()}\n"
            f"Cantidad: {quantity}\n"
            f"Motivo: {reason}"
        )
