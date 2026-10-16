"""Configuración centralizada del MCP Server mediante variables de entorno."""

import os


class Settings:
    def __init__(self) -> None:
        self.inventory_service_url: str = os.getenv(
            "INVENTORY_SERVICE_URL", "http://inventory-service:8080"
        )
        self.ai_service_url: str = os.getenv(
            "AI_SERVICE_URL", "http://ai-service:8000"
        )
        self.internal_api_key: str = os.getenv(
            "INTERNAL_API_KEY", "neuroinventory-secret-key-2026"
        )
        self.mcp_host: str = os.getenv("MCP_HOST", "0.0.0.0")
        self.mcp_port: int = int(os.getenv("MCP_PORT", "8001"))
        self.service_token: str = os.getenv("SERVICE_TOKEN", "")


settings = Settings()
