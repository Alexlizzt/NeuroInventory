# Configuración de variables de entorno — NeuroInventory

Esta guía explica paso a paso cómo un desarrollador debe generar y configurar las variables de entorno necesarias para levantar el proyecto en su máquina local.

---

## 1. Preparar el archivo `.env`

Desde la raíz del repositorio:

```bash
cp .env.example .env
```

El archivo `.env.example` ya contiene todos los valores por defecto para desarrollo local. Solo necesitas ajustar lo que sea necesario y, sobre todo, **generar el `SERVICE_TOKEN`** si vas a usar el MCP Server.

> ⚠️ Nunca subas el archivo `.env` a control de versiones. Está incluido en `.gitignore`.

---

## 2. Variables por servicio y cómo generarlas

A continuación se detalla cada bloque de variables, para qué sirve y cómo obtener el valor real si no usas el por defecto.

### 2.1 PostgreSQL + pgvector

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `POSTGRES_DB` | `neuroinventory` | Nombre de la base de datos. |
| `POSTGRES_USER` | `postgres` | Usuario administrador. |
| `POSTGRES_PASSWORD` | `secretpassword` | Contraseña del usuario administrador. |
| `POSTGRES_PORT` | `5432` | Puerto expuesto en localhost. |

No necesitas generar nada aquí; los valores por defecto funcionan para desarrollo local.

### 2.2 Keycloak (Identidad y seguridad)

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `KEYCLOAK_VERSION` | `24.0` | Versión de la imagen de Keycloak. |
| `KEYCLOAK_CONFIG_CLI_VERSION` | `latest-24` | Versión del importador de realm. |
| `KEYCLOAK_DB` | `keycloak` | Base de datos interna de Keycloak. |
| `KC_BOOTSTRAP_ADMIN_USERNAME` | `admin` | Usuario admin de Keycloak. |
| `KC_BOOTSTRAP_ADMIN_PASSWORD` | `admin1234!` | Contraseña admin de Keycloak. |
| `KEYCLOAK_PORT` | `9090` | Puerto expuesto en localhost. |
| `KEYCLOAK_REALM` | `neuroinventory` | Nombre del realm importado desde `config/realm.json`. |

**Cómo acceder:** Una vez levantados los servicios, la consola de administración queda en `http://localhost:9090`.

### 2.3 Ollama (LLM local y embeddings)

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `OLLAMA_PORT` | `11434` | Puerto expuesto en localhost. |
| `OLLAMA_HOST` | `http://ollama:11434` | URL interna del contenedor (usa este valor tal cual en Docker). |
| `OLLAMA_VERSION` | `0.40.11` | Versión de la imagen oficial de Ollama. |
| `LLM_MODEL` | `llama3.2` | Modelo de lenguaje a descargar. |
| `EMBEDDING_MODEL` | `nomic-embed-text` | Modelo de embeddings a descargar. |

Los modelos se descargan la primera vez con el servicio `ollama-models` y quedan
guardados en el volumen `ollama_data`. Los siguientes arranques los reutilizan y
no vuelven a descargarlos.

### 2.4 AI Service (FastAPI)

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `AI_SERVICE_PORT` | `8000` | Puerto expuesto en localhost. |
| `AI_SERVICE_HOST` | `ai-service` | Host interno de Docker (usa este valor tal cual). |
| `DATABASE_URL` | `postgresql://postgres:secretpassword@postgres:5432/neuroinventory` | Cadena de conexión a PostgreSQL. Ajusta si cambias `POSTGRES_PASSWORD`. |
| `INTERNAL_API_KEY` | `neuroinventory-secret-key-2026` | Clave compartida entre `inventory-service`, `mcp-server` y `ai-service`. |

### 2.5 Inventory Service (Spring Boot)

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `INVENTORY_SERVICE_PORT` | `8080` | Puerto expuesto en localhost. |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://postgres:5432/neuroinventory` | JDBC a PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuario de BD. |
| `SPRING_DATASOURCE_PASSWORD` | `secretpassword` | Contraseña de BD. |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | `http://localhost:9090/realms/neuroinventory` | Emisor de JWT de Keycloak. |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | `http://keycloak:8080/realms/neuroinventory/protocol/openid-connect/certs` | JWK Set interno de Docker. |
| `BASE_PROBLEM_URI` | `http://localhost:8080/problem` | URI base para errores RFC 9457. |

### 2.6 MCP Server (Model Context Protocol)

| Variable | Valor por defecto | Notas |
|----------|-------------------|-------|
| `MCP_SERVER_PORT` | `8001` | Puerto del servidor MCP (SSE). |
| `SERVICE_TOKEN` | *(vacío)* | **JWT del service account** para agentes autónomos. |

#### ¿Cómo generar `SERVICE_TOKEN`?

El realm `config/realm.json` ya incluye el cliente `mcp-server-client` con service accounts habilitados y un usuario vinculado con rol `ROLE_ADMIN`.

1. Levanta al menos `postgres`, `keycloak` y `keycloak-config-cli`:

   ```bash
   docker compose up -d postgres keycloak keycloak-config-cli
   ```

2. Obtén el token mediante **Client Credentials**:

   ```bash
   curl -X POST "http://localhost:9090/realms/neuroinventory/protocol/openid-connect/token" \
     -d "grant_type=client_credentials" \
     -d "client_id=mcp-server-client" \
     -d "client_secret=mcp-server-secret"
   ```

3. La respuesta será algo como:

   ```json
   {
     "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
     "expires_in": 300,
     "refresh_expires_in": 1800,
     "token_type": "Bearer"
   }
   ```

4. Copia **solo el valor de `access_token`** (sin `Bearer ` ni comillas) en tu `.env`:

   ```env
   SERVICE_TOKEN=eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...
   ```

> ⚠️ Los JWT expiran (por defecto a los 300 segundos en esta configuración). Si el token caduca, las llamadas del MCP Server al `inventory-service` fallarán con 401. Renueva el token ejecutando el mismo `curl` de arriba.

---

## 3. Ejemplo completo de `.env` para desarrollo

```env
# ============================================================================
# PostgreSQL
# ============================================================================
POSTGRES_DB=neuroinventory
POSTGRES_HOST=postgres
POSTGRES_USER=postgres
POSTGRES_PASSWORD=secretpassword
POSTGRES_PORT=5432

# ============================================================================
# Keycloak
# ============================================================================
KEYCLOAK_VERSION=24.0
KEYCLOAK_CONFIG_CLI_VERSION=latest-24
KEYCLOAK_DB=keycloak
KC_BOOTSTRAP_ADMIN_USERNAME=admin
KC_BOOTSTRAP_ADMIN_PASSWORD=admin1234!
KEYCLOAK_PORT=9090
KEYCLOAK_REALM=neuroinventory

# ============================================================================
# Ollama
# ============================================================================
OLLAMA_PORT=11434
OLLAMA_HOST=http://ollama:11434
OLLAMA_VERSION=0.40.11
LLM_MODEL=llama3.2
EMBEDDING_MODEL=nomic-embed-text

# ============================================================================
# AI Service
# ============================================================================
AI_SERVICE_PORT=8000
AI_SERVICE_HOST=ai-service
DATABASE_URL=postgresql://postgres:secretpassword@postgres:5432/neuroinventory
INTERNAL_API_KEY=neuroinventory-secret-key-2026

# ============================================================================
# MCP Server
# ============================================================================
MCP_SERVER_PORT=8001
SERVICE_TOKEN=

# ============================================================================
# Frontend
# ============================================================================
FRONTEND_PORT=4200

# ============================================================================
# Inventory Service
# ============================================================================
INVENTORY_SERVICE_PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/neuroinventory
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secretpassword
BASE_PROBLEM_URI=http://localhost:8080/problem
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:9090/realms/neuroinventory
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=http://keycloak:8080/realms/neuroinventory/protocol/openid-connect/certs

# ============================================================================
# Observabilidad (OpenTelemetry)
# ============================================================================
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://otel-collector:4318
OTEL_SERVICE_NAME=neuroinventory

# Solo con docker-compose.observability.yml
GRAFANA_ADMIN_USER=admin
GRAFANA_ADMIN_PASSWORD=admin
PROMETHEUS_PORT=9091
PROMETHEUS_RETENTION=15d
```

---

## 4. Levantar todos los servicios

Desde la raíz del proyecto:

```bash
docker compose up --build
```

### URLs de acceso una vez levantado (desarrollo directo)

| Servicio | URL |
|----------|-----|
| Frontend Angular | http://localhost:4200 |
| Inventory Service (API) | http://localhost:8080/api/v1/inventory-service |
| AI Service (API) | http://localhost:8000 |
| MCP Server (SSE) | http://localhost:8001/sse |
| Keycloak (admin) | http://localhost:9090 |
| Prometheus | http://localhost:9091 |
| Grafana | http://localhost:3000 (admin / admin) |

---

## 5. Verificación rápida

```bash
# Health checks
curl http://localhost:8080/api/v1/inventory-service/actuator/health  # Inventory Service
curl http://localhost:8000/health                    # AI Service
curl http://localhost:8001/sse                       # MCP Server
curl http://localhost:9090/realms/neuroinventory     # Keycloak
```

---

## 6. Notas de seguridad para desarrollo

| Recomendación | Razón |
|---------------|-------|
| No uses `.env` en producción | Genera secretos fuertes y usa un gestor de secretos (Vault, AWS Secrets Manager, etc.). |
| `SERVICE_TOKEN` expira | Renúvalo periódicamente o usa `access_token` por petición para usuarios finales. |
| `INTERNAL_API_KEY` | Cambia el valor por defecto si expón los servicios fuera de localhost. |
| `POSTGRES_PASSWORD` | No uses `secretpassword` en entornos que no sean desarrollo local. |

---

## 7. ¿Qué hacer si algo falla?

| Problema | Solución |
|----------|----------|
| `keycloak-config-cli` no importa el realm | Espera a que Keycloak esté sano (`service_healthy`) y revisa logs: `docker compose logs keycloak-config-cli`. |
| `mcp-server` no puede llamar a `inventory-service` | Verifica que `SERVICE_TOKEN` sea válido y no haya expirado. |
| `inventory-service` rechaza el JWT | Confirma que `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` apunte al realm correcto. |
| Ollama tarda mucho en arrancar | Es normal la primera vez: el servicio `ollama-models` descarga `llama3.2` y `nomic-embed-text` en el volumen `ollama_data`. Revisa `docker compose logs ollama-models`. |

---

## 8. Resumen rápido para el desarrollador

1. `cp .env.example .env`
2. (Opcional) Ajusta contraseñas y puertos si es necesario.
3. Genera `SERVICE_TOKEN` con `client_credentials` contra Keycloak si usarás el MCP Server.
4. `docker compose up --build`
5. Accede a `http://localhost:4200` (frontend) y prueba el flujo completo.

### Levantar observabilidad (OpenTelemetry + Prometheus + Grafana)

```bash
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build
```

Luego accede a:
- **Grafana**: http://localhost:3000 (admin / admin)
- **Prometheus**: http://localhost:9091