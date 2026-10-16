#!/usr/bin/env bash
#
# generate-service-token.sh
#
# Obtiene un JWT de Keycloak mediante Client Credentials para el cliente
# mcp-server-client y lo escribe en la variable SERVICE_TOKEN del archivo .env.
#
# Uso:
#   ./scripts/generate-service-token.sh [--apply]
#
# Opciones:
#   --apply   Además de escribir el .env, reinicia el servicio mcp-server
#             para que tome el nuevo token sin tocar el resto del stack.
#
# Requisitos:
#   - curl
#   - python3 (para parsear la respuesta JSON de Keycloak)
#   - docker compose (solo si se pasa --apply)
#

set -euo pipefail

APPLY=false

if [[ "${1:-}" == "--apply" ]]; then
  APPLY=true
fi

# ---------------------------------------------------------------------------
# Configuración (ajusta si cambias .env o realm.json)
# ---------------------------------------------------------------------------
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:9090}"
REALM="${KEYCLOAK_REALM:-neuroinventory}"
CLIENT_ID="${MCP_CLIENT_ID:-mcp-server-client}"
CLIENT_SECRET="${MCP_CLIENT_SECRET:-mcp-server-secret}"
ENV_FILE="${ENV_FILE:-.env}"
COMPOSE_SERVICE="${COMPOSE_SERVICE:-mcp-server}"

TOKEN_URL="${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token"

echo "🔑 Solicitando token a Keycloak..."
echo "   URL: ${TOKEN_URL}"
echo "   Cliente: ${CLIENT_ID}"

# ---------------------------------------------------------------------------
# 1. Pedir el token a Keycloak (Client Credentials)
# ---------------------------------------------------------------------------
RESPONSE=$(curl -s -X POST "${TOKEN_URL}" \
  -d "grant_type=client_credentials" \
  -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}")

HTTP_CODE=$(echo "${RESPONSE}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('error','ok'))" 2>/dev/null || echo "ok")

if echo "${RESPONSE}" | grep -q '"error"'; then
  ERROR_DESC=$(echo "${RESPONSE}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('error_description','Error desconocido'))" 2>/dev/null || echo "Error desconocido")
  echo "❌ Error obteniendo el token: ${ERROR_DESC}"
  exit 1
fi

ACCESS_TOKEN=$(echo "${RESPONSE}" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

if [[ -z "${ACCESS_TOKEN}" ]]; then
  echo "❌ No se pudo extraer el access_token de la respuesta."
  exit 1
fi

EXPIRES_IN=$(echo "${RESPONSE}" | python3 -c "import sys,json; print(json.load(sys.stdin).get('expires_in','desconocido'))" 2>/dev/null || echo "desconocido")

echo "✅ Token obtenido (expires_in: ${EXPIRES_IN} s)"

# ---------------------------------------------------------------------------
# 2. Escribir SERVICE_TOKEN en el archivo .env
# ---------------------------------------------------------------------------
if [[ ! -f "${ENV_FILE}" ]]; then
  echo "⚠️  No existe ${ENV_FILE}. Copiando desde .env.example..."
  cp .env.example "${ENV_FILE}"
fi

# Eliminar línea previa de SERVICE_TOKEN si existe
if grep -q '^SERVICE_TOKEN=' "${ENV_FILE}"; then
  sed -i.bak "/^SERVICE_TOKEN=/d" "${ENV_FILE}"
  rm -f "${ENV_FILE}.bak"
fi

# Agregar la nueva línea al final (o después del bloque MCP si prefieres orden)
echo "" >> "${ENV_FILE}"
echo "# Token generado automáticamente el $(date -u +%Y-%m-%dT%H:%M:%SZ)" >> "${ENV_FILE}"
echo "SERVICE_TOKEN=${ACCESS_TOKEN}" >> "${ENV_FILE}"

echo "💾 Token guardado en ${ENV_FILE}"

# ---------------------------------------------------------------------------
# 3. Opcionalmente reiniciar solo el servicio mcp-server
# ---------------------------------------------------------------------------
if [[ "${APPLY}" == true ]]; then
  echo "🔄 Reiniciando servicio '${COMPOSE_SERVICE}'..."
  if ! docker compose up -d --build "${COMPOSE_SERVICE}"; then
    echo "❌ No se pudo reiniciar '${COMPOSE_SERVICE}' con docker compose."
    exit 1
  fi
  echo "✅ Servicio '${COMPOSE_SERVICE}' reiniciado con el nuevo token."
else
  echo "💡 Para aplicar el nuevo token, ejecuta:"
  echo "   docker compose up -d --build mcp-server"
fi
