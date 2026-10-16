# MCP Server — NeuroInventory

Servidor **Model Context Protocol (MCP)** que expone las capacidades del dominio de NeuroInventory como herramientas para agentes de IA.

Está alineado con el **ADR-0007: Adoptar MCP para integración con agentes IA**.

## Arquitectura

El MCP Server actúa como capa de adaptación entre agentes externos y los servicios internos:

```text
                         AI Agent

                             |

                          MCP

                             |

                      MCP Server

                             |
        ┌────────────────────┴────────────────────┐
        |                                         |

Inventory Service                         AI Service

(productos, stock, movimientos)           (búsqueda semántica, RAG)
```

El MCP Server **no contiene reglas de negocio**. Traduce llamadas MCP a operaciones existentes de los microservicios.

## Herramientas expuestas

| Herramienta | Descripción | Servicio destino |
|-------------|-------------|------------------|
| `buscar_producto` | Búsqueda semántica de productos por lenguaje natural | AI Service (`/api/v1/search/semantic`) |
| `consultar_stock` | Consulta disponibilidad de stock de un producto | Inventory Service (`/inventory/stock/{id}`) |
| `crear_movimiento` | Registra entrada/salida de inventario | Inventory Service (`/inventory/movements`) |
| `consultar_documentacion` | Consulta documentación técnica mediante RAG | AI Service (`/api/v1/rag/query`) |

## Variables de entorno

| Variable | Descripción | Por defecto |
|----------|-------------|-------------|
| `INVENTORY_SERVICE_URL` | URL base del Inventory Service | `http://inventory-service:8080` |
| `AI_SERVICE_URL` | URL base del AI Service | `http://ai-service:8000` |
| `INTERNAL_API_KEY` | Clave interna para llamar al AI Service | `neuroinventory-secret-key-2026` |
| `MCP_HOST` | Host donde escucha el servidor | `0.0.0.0` |
| `MCP_PORT` | Puerto del servidor MCP | `8001` |
| `SERVICE_TOKEN` | Token JWT opcional para llamar a Inventory Service (agente autónomo). Se usa si no se pasa `access_token` en la herramienta. | `` |

> **`SERVICE_TOKEN` mediante service account:** El realm (`config/realm.json`) ya incluye el cliente `mcp-server-client` con service accounts habilitados y su usuario vinculado (`service-account-mcp-server-client`) con rol `ROLE_ADMIN`. Obtén el JWT con Client Credentials y colócalo en `SERVICE_TOKEN`:

```bash
curl -X POST "http://localhost:9090/realms/neuroinventory/protocol/openid-connect/token" \
  -d "grant_type=client_credentials" \
  -d "client_id=mcp-server-client" \
  -d "client_secret=mcp-server-secret"
```

Copia el `access_token` resultante en tu `.env`:

```env
SERVICE_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...tu_jwt_aquí...
```

> El service account tiene rol `ROLE_ADMIN`. Si necesitas algo más restrictivo, cambia `realmRoles` en `config/realm.json` a `["ROLE_USER"]` (ten en cuenta que `crear_movimiento` requiere `ROLE_ADMIN`).

## Ejecución local

```bash
cd mcp-server
pip install -r requirements.txt
python main.py
```

El servidor escucha por transporte **SSE** en `http://localhost:8001/sse`.

## Integración con Docker Compose

El servicio ya está incluido en `docker-compose.yml` bajo el nombre `mcp-server` y queda disponible en la red interna `neuroinventory-net` en el puerto `8001`.

---

## 🔐 Propagación de la identidad del usuario (agentes IA)

El `inventory-service` está protegido por **OAuth2 / OIDC mediante Keycloak** (JWT).
Las herramientas MCP que acceden a él —`consultar_stock` y `crear_movimiento`— requieren
un token válido. Por diseño del ADR-0007, el MCP Server **no almacena ni gestiona**
credenciales de usuario; la identidad debe propagarse desde el cliente/agente.

### Cómo funciona el flujo de identidad

```text
Usuario/Agente
     │
     ├─► Keycloak: login → recibe Access Token (JWT)
     │
     ├─► MCP Server: llama herramienta pasando access_token
     │         (ej. consultar_stock({product_id, access_token}))
     │
     ▼
MCP Server reenvía el JWT como Authorization: Bearer <token>
     │
     ▼
Inventory Service valida el JWT contra Keycloak (JWK Set)
     │
     ▼
Operación ejecutada con identidad y roles del usuario
```

### Desde un cliente MCP

El cliente MCP debe:

1. **Autenticar al usuario** contra Keycloak y obtener un `access_token` (JWT).
   * Desde un navegador/fronend: Authorization Code Flow con PKCE.
   * Desde un agente sin intervención de usuario: Client Credentials o Resource Owner Password Credentials (solo entornos de prueba).
2. **Pasar el `access_token`** en las llamadas a las herramientas protegidas:
   * `consultar_stock({ "product_id": "...", "access_token": "<jwt>" })`
   * `crear_movimiento({ "product_id": "...", "movement_type": "IN", "quantity": 10, "reason": "...", "access_token": "<jwt>" })`

Si no se provee `access_token`, el MCP Server intenta usar `SERVICE_TOKEN`
(variable de entorno del servidor). Esto solo se recomienda para agentes
autónomos con una identidad de servicio fija, no para usuarios finales.

### Ejemplo de cliente con autenticación

Ver [`examples/client_with_auth.py`](examples/client_with_auth.py). Muestra cómo:

1. Obtener un JWT de Keycloak.
2. Conectarse al MCP Server por SSE.
3. Invocar `consultar_stock` y `crear_movimiento` pasando el `access_token`.

### Seguridad

* El MCP Server **no valida** el JWT; el `inventory-service` es quien valida la firma
  contra el JWK Set de Keycloak.
* La red interna `neuroinventory-net` aísla la comunicación; solo los puertos
  publicados en `docker-compose.yml` son accesibles desde el host.
* Las herramientas expuestas no permiten acceso directo a la base de datos ni a
  servicios internos no autorizados.

