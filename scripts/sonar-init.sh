#!/usr/bin/env bash
# =============================================================================
# Inicializa el SonarQube Community de docker-compose.jenkins.yml.
#
#   1. Espera a que el servidor esté UP.
#   2. Ajusta la contraseña del usuario `admin` a SONAR_ADMIN_PASSWORD.
#   3. Genera el token de análisis `jenkins` y lo guarda en .env.jenkins.
#
# No registra webhooks: SonarQube bloquea URLs internas por defecto
# (`sonar.validateWebhooks`) y el pipeline consulta el quality gate por API.
#
# Uso:
#   cp .env.jenkins.example .env.jenkins   # define SONAR_ADMIN_PASSWORD
#   ./scripts/sonar-init.sh
#   docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins \
#       up -d --force-recreate jenkins
#
# El script es idempotente: si el admin ya tiene la clave indicada, no la
# cambia; el token `jenkins` se regenera en cada ejecución.
# =============================================================================

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${SONAR_ENV_FILE:-$ROOT_DIR/.env.jenkins}"

if [ -f "$ENV_FILE" ]; then
    set -a
    # shellcheck disable=SC1090
    . "$ENV_FILE"
    set +a
fi

SONAR_URL="${SONAR_URL:-http://localhost:${SONAR_PORT:-9000}}"
SONAR_URL="${SONAR_URL%/}"
ADMIN_LOGIN="${SONAR_ADMIN_LOGIN:-admin}"
ADMIN_PASSWORD="${SONAR_ADMIN_PASSWORD:-}"
TOKEN_NAME="${SONAR_TOKEN_NAME:-jenkins}"

if [ -z "$ADMIN_PASSWORD" ]; then
    echo "ERROR: define SONAR_ADMIN_PASSWORD en $ENV_FILE (mínimo 12 caracteres)." >&2
    exit 1
fi

# SonarQube acepta autenticación básica con usuario/contraseña o con un token
# como usuario. Todos los endpoints se llaman con la clave del admin.
api() {
    local method="$1" path="$2" user="$3"
    shift 3
    curl -fsS -X "$method" -u "$user" "$@" "$SONAR_URL$path"
}

wait_for_sonar() {
    echo "Esperando a SonarQube en $SONAR_URL ..."
    for _ in $(seq 1 120); do
        if curl -fsS "$SONAR_URL/api/system/status" 2>/dev/null | grep -q '"status":"UP"'; then
            echo "SonarQube está listo."
            return 0
        fi
        sleep 5
    done
    echo "ERROR: SonarQube no respondió. Revisa 'docker compose -f docker-compose.jenkins.yml logs sonarqube'." >&2
    exit 1
}

credentials_work() {
    api GET "/api/users/current" "$1:$2" >/dev/null 2>&1
}

change_password() {
    api POST "/api/users/change_password" "$ADMIN_LOGIN:$1" \
        --data-urlencode "login=$ADMIN_LOGIN" \
        --data-urlencode "password=$ADMIN_PASSWORD" \
        --data-urlencode "previousPassword=$1" >/dev/null
}

wait_for_sonar

if credentials_work "$ADMIN_LOGIN" "$ADMIN_PASSWORD"; then
    echo "La contraseña de '$ADMIN_LOGIN' ya está actualizada."
else
    echo "La contraseña indicada no es válida; probando la inicial 'admin' ..."
    if ! credentials_work "$ADMIN_LOGIN" "admin"; then
        echo "ERROR: no se pudo autenticar como '$ADMIN_LOGIN' (ni con SONAR_ADMIN_PASSWORD ni con 'admin')." >&2
        exit 1
    fi
    change_password "admin"
    echo "Contraseña de '$ADMIN_LOGIN' actualizada."
fi

# El token se regenera para que el valor de .env.jenkins siempre sea el válido.
api POST "/api/user_tokens/revoke" "$ADMIN_LOGIN:$ADMIN_PASSWORD" \
    --data-urlencode "name=$TOKEN_NAME" >/dev/null 2>&1 || true

TOKEN="$(api POST "/api/user_tokens/generate" "$ADMIN_LOGIN:$ADMIN_PASSWORD" \
    --data-urlencode "name=$TOKEN_NAME" \
    | python3 -c 'import json, sys; print(json.load(sys.stdin)["token"])')"

if [ -f "$ENV_FILE" ]; then
    if grep -q '^SONAR_TOKEN=' "$ENV_FILE"; then
        sed -i "s|^SONAR_TOKEN=.*|SONAR_TOKEN=$TOKEN|" "$ENV_FILE"
    else
        printf '\nSONAR_TOKEN=%s\n' "$TOKEN" >> "$ENV_FILE"
    fi
    echo "SONAR_TOKEN guardado en $ENV_FILE."
else
    echo "SONAR_TOKEN (guárdalo, no se vuelve a mostrar): $TOKEN"
fi

cat <<EOF

Listo. Recrea Jenkins para que JCasC cargue la credencial:
  docker compose -f docker-compose.jenkins.yml --env-file .env.jenkins up -d --force-recreate jenkins

SonarQube: $SONAR_URL  (usuario: $ADMIN_LOGIN)
EOF
