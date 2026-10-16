"""Cliente para comunicarse con el Inventory Service de NeuroInventory.

Los endpoints del Inventory Service están protegidos por OAuth2/JWT (Keycloak).
Por eso cada llamada acepta un `access_token` opcional que se reenvía como
`Authorization: Bearer <token>`. Si no se provee, se intenta usar `SERVICE_TOKEN`
configurado en el MCP Server.
"""

from typing import Any, Dict, Optional

import httpx


class InventoryClient:
    def __init__(self, base_url: str, service_token: str = "") -> None:
        self.base_url = base_url.rstrip("/")
        # Token de servicio de respaldo (agentes autónomos sin usuario final).
        # En llamadas de usuario final se prefiere el access_token por petición.
        self.service_token = service_token

    def _resolve_token(self, access_token: Optional[str] = None) -> Optional[str]:
        """Resuelve el token JWT a enviar al Inventory Service.

        Prioridad:
          1. access_token pasado explícitamente en la herramienta MCP (identidad del usuario).
          2. SERVICE_TOKEN del servidor MCP (identidad de servicio / agente autónomo).
        """
        return access_token or self.service_token or None

    def _headers(self, access_token: Optional[str] = None) -> Dict[str, str]:
        headers: Dict[str, str] = {"Content-Type": "application/json"}
        token = self._resolve_token(access_token)
        if token:
            headers["Authorization"] = f"Bearer {token}"
        return headers

    async def get_stock(
        self, product_id: str, access_token: Optional[str] = None
    ) -> Dict[str, Any]:
        """Consulta el stock de un producto.

        Endpoint: GET /inventory/stock/{productId}
        """
        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.get(
                f"{self.base_url}/inventory/stock/{product_id}",
                headers=self._headers(access_token),
            )
            response.raise_for_status()
            return response.json()

    async def create_movement(
        self,
        product_id: str,
        movement_type: str,
        quantity: int,
        reason: str,
        access_token: Optional[str] = None,
    ) -> Dict[str, Any]:
        """Registra un movimiento de inventario (entrada/salida/ajuste).

        Endpoint: POST /inventory/movements

        Nota: el `user_id` no se envía en el cuerpo HTTP porque el Inventory Service
        lo inyecta desde el contexto de seguridad (JWT/Keycloak) de la petición.
        """
        payload = {
            "productId": product_id,
            "type": movement_type,
            "quantity": quantity,
            "reason": reason,
        }

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post(
                f"{self.base_url}/inventory/movements",
                json=payload,
                headers=self._headers(access_token),
            )
            response.raise_for_status()
            return response.json()
