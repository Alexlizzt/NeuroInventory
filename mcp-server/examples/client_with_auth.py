"""Ejemplo de cliente MCP que propaga la identidad del usuario.

Este script muestra cómo un agente o cliente MCP puede:
1. Autenticarse contra Keycloak (Authorization Code Flow / Client Credentials).
2. Obtener un access token (JWT).
3. Conectarse al servidor MCP de NeuroInventory.
4. Invocar herramientas pasando el access_token para que la identidad
   del usuario se propague al Inventory Service (consultar_stock, crear_movimiento).
"""

import asyncio
import os
from typing import Any, Dict

import httpx

# Cliente MCP (asume que mcp está instalado: pip install mcp)
from mcp.client.sse import sse_client
from mcp import ClientSession


KEYCLOAK_TOKEN_URL = os.getenv(
    "KEYCLOAK_TOKEN_URL",
    "http://localhost:9090/realms/neuroinventory/protocol/openid-connect/token",
)
KEYCLOAK_CLIENT_ID = os.getenv("KEYCLOAK_CLIENT_ID", "frontend-client")
KEYCLOAK_CLIENT_SECRET = os.getenv("KEYCLOAK_CLIENT_SECRET", "")
KEYCLOAK_USERNAME = os.getenv("KEYCLOAK_USERNAME", "usuario")
KEYCLOAK_PASSWORD = os.getenv("KEYCLOAK_PASSWORD", "password")

MCP_SERVER_URL = os.getenv("MCP_SERVER_URL", "http://localhost:8001/sse")


async def obtener_access_token() -> str:
    """Obtiene un JWT de Keycloak usando Resource Owner Password Credentials.

    En producción se recomienda Authorization Code Flow (PKCE) desde el frontend
    o Client Credentials para agentes sin intervención de usuario.
    """
    async with httpx.AsyncClient(timeout=30.0) as client:
        data = {
            "grant_type": "password",
            "client_id": KEYCLOAK_CLIENT_ID,
            "username": KEYCLOAK_USERNAME,
            "password": KEYCLOAK_PASSWORD,
        }
        if KEYCLOAK_CLIENT_SECRET:
            data["client_secret"] = KEYCLOAK_CLIENT_SECRET

        resp = await client.post(KEYCLOAK_TOKEN_URL, data=data)
        resp.raise_for_status()
        return resp.json()["access_token"]


async def main() -> None:
    # 1. Autenticación del usuario / agente
    access_token = await obtener_access_token()
    print(f"✅ Token obtenido (sub se extraerá del JWT en el backend)")

    # 2. Conexión al servidor MCP
    async with sse_client(MCP_SERVER_URL) as (read, write):
        async with ClientSession(read, write) as session:
            await session.initialize()

            # Listar herramientas disponibles
            tools = await session.list_tools()
            print("🛠 Herramientas disponibles:")
            for t in tools.tools:
                print(f"   - {t.name}: {t.description}")

            # 3. Ejemplo: consultar_stock propagando el token del usuario
            print("\n📦 Consultando stock con identidad de usuario...")
            result_stock = await session.call_tool(
                "consultar_stock",
                {
                    "product_id": "prod-123456",
                    "access_token": access_token,  # ← identidad propagada
                },
            )
            print(result_stock.content[0].text if result_stock.content else result_stock)

            # 4. Ejemplo: crear_movimiento propagando el token del usuario
            print("\n📥 Registrando movimiento con identidad de usuario...")
            result_mov = await session.call_tool(
                "crear_movimiento",
                {
                    "product_id": "prod-123456",
                    "movement_type": "IN",
                    "quantity": 10,
                    "reason": "Reposición de stock por compra",
                    "access_token": access_token,  # ← identidad propagada
                },
            )
            print(result_mov.content[0].text if result_mov.content else result_mov)

            # 5. Ejemplo: buscar_producto (no requiere token del usuario,
            #    solo la API Key interna del AI Service)
            print("\n🔍 Búsqueda semántica (sin token de usuario necesario)...")
            result_search = await session.call_tool(
                "buscar_producto",
                {"query": "laptop liviana para programar", "limit": 3},
            )
            print(result_search.content[0].text if result_search.content else result_search)


if __name__ == "__main__":
    asyncio.run(main())
