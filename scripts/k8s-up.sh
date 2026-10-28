#!/usr/bin/env bash
# =============================================================================
# Despliegue local de NeuroInventory en minikube.
#
#   ./scripts/k8s-up.sh              # construye solo lo que falte y despliega
#   ./scripts/k8s-up.sh --build      # reconstruye y recarga todas las imágenes
#
# Variables opcionales:
#   FRONTEND_HOST=neuroinventory.local
#   KEYCLOAK_HOST=auth.neuroinventory.local
#   NAMESPACE=neuroinventory
#   SKIP_HOSTS=1   # no modifica /etc/hosts
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

NAMESPACE="${NAMESPACE:-neuroinventory}"
FRONTEND_HOST="${FRONTEND_HOST:-neuroinventory.local}"
KEYCLOAK_HOST="${KEYCLOAK_HOST:-auth.neuroinventory.local}"

BUILD=0
if [[ "${1:-}" == "--build" ]]; then
  BUILD=1
fi

for cmd in docker minikube kubectl; do
  command -v "$cmd" >/dev/null 2>&1 || {
    echo "ERROR: falta el comando '$cmd'." >&2
    exit 1
  }
done

# Valores por defecto tomados de .env (sin sobrescribir variables ya definidas).
if [[ -f .env ]]; then
  set -a
  # shellcheck disable=SC1091
  source .env
  set +a
fi

POSTGRES_DB="${POSTGRES_DB:-neuroinventory}"
POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-secretpassword}"
KEYCLOAK_DB="${KEYCLOAK_DB:-keycloak}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-neuroinventory}"
KC_BOOTSTRAP_ADMIN_USERNAME="${KC_BOOTSTRAP_ADMIN_USERNAME:-admin}"
KC_BOOTSTRAP_ADMIN_PASSWORD="${KC_BOOTSTRAP_ADMIN_PASSWORD:-admin1234!}"
INTERNAL_API_KEY="${INTERNAL_API_KEY:-neuroinventory-secret-key-2026}"
SERVICE_TOKEN="${SERVICE_TOKEN:-}"
LLM_MODEL="${LLM_MODEL:-llama3.2}"
EMBEDDING_MODEL="${EMBEDDING_MODEL:-nomic-embed-text}"

echo "==> Comprobando minikube"
minikube status >/dev/null 2>&1 || {
  echo "ERROR: minikube no está arrancado. Ejecuta 'minikube start'." >&2
  exit 1
}

echo "==> Habilitando el addon ingress"
minikube addons enable ingress >/dev/null
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=600s >/dev/null

# -----------------------------------------------------------------------------
# Imágenes
# -----------------------------------------------------------------------------
build_image() {
  local ref="$1" context="$2"

  if [[ "$BUILD" == "1" ]] || ! docker image inspect "$ref" >/dev/null 2>&1; then
    echo "==> Construyendo $ref"
    docker build -t "$ref" "$context"
  fi
}

image_loaded() {
  minikube image ls 2>/dev/null | grep -Fq "$1"
}

load_image() {
  local ref="$1" force="${2:-0}"

  if [[ "$force" == "1" ]] || ! image_loaded "$ref"; then
    echo "==> Cargando $ref en minikube"
    # minikube no reemplaza una imagen que ya existe con el mismo tag, ni con
    # --overwrite: hay que eliminarla del nodo antes de cargar la nueva.
    minikube ssh -- docker rmi -f "$ref" >/dev/null 2>&1 || true
    minikube image load "$ref"
  fi
}

build_image "neuroinventory/ai-service:k8s" "$ROOT/ai-service"
build_image "neuroinventory/mcp-server:k8s" "$ROOT/mcp-server"
build_image "neuroinventory/inventory-service:k8s" "$ROOT/inventory-service"
# Sin target: usa el stage final (Nginx + bundle de producción).
build_image "neuroinventory/frontend:k8s" "$ROOT/frontend"

# Las imágenes propias se recargan siempre que se pida --build.
load_image "neuroinventory/ai-service:k8s" "$BUILD"
load_image "neuroinventory/mcp-server:k8s" "$BUILD"
load_image "neuroinventory/inventory-service:k8s" "$BUILD"
load_image "neuroinventory/frontend:k8s" "$BUILD"

# Servicios de infraestructura: si ya existen en el Docker del host se cargan
# para evitar descargas; si no, Kubernetes los descargará del registro.
for ref in \
  "pgvector/pgvector:pg16" \
  "quay.io/keycloak/keycloak:24.0" \
  "adorsys/keycloak-config-cli:latest-24" \
  "ollama/ollama:0.40.11" \
  "busybox:1.36"; do
  if docker image inspect "$ref" >/dev/null 2>&1; then
    load_image "$ref"
  fi
done

# -----------------------------------------------------------------------------
# Namespace, ConfigMaps y Secret
# -----------------------------------------------------------------------------
echo "==> Creando namespace y configuración"
kubectl apply -f "$ROOT/deploy/k8s/namespace.yaml"

# Se guarda el hash de la configuración anterior para reiniciar solo los
# Deployments cuyas variables hayan cambiado (por ejemplo al cambiar de host).
data_hash() {
  local kind="$1" name="$2"

  if kubectl -n "$NAMESPACE" get "$kind" "$name" >/dev/null 2>&1; then
    kubectl -n "$NAMESPACE" get "$kind" "$name" -o jsonpath='{.data}' \
      | sha256sum | cut -d' ' -f1
  fi
}

OLD_APP_HASH="$(data_hash configmap app-config)"
OLD_FRONTEND_HASH="$(data_hash configmap frontend-config)"
OLD_SECRET_HASH="$(data_hash secret neuroinventory-secrets)"

kubectl -n "$NAMESPACE" create configmap app-config \
  --from-literal=POSTGRES_DB="$POSTGRES_DB" \
  --from-literal=POSTGRES_USER="$POSTGRES_USER" \
  --from-literal=POSTGRES_HOST="postgres" \
  --from-literal=POSTGRES_PORT="5432" \
  --from-literal=KEYCLOAK_DB="$KEYCLOAK_DB" \
  --from-literal=KEYCLOAK_REALM="$KEYCLOAK_REALM" \
  --from-literal=KEYCLOAK_HOST="$KEYCLOAK_HOST" \
  --from-literal=FRONTEND_HOST="$FRONTEND_HOST" \
  --from-literal=OLLAMA_HOST="http://ollama:11434" \
  --from-literal=LLM_MODEL="$LLM_MODEL" \
  --from-literal=EMBEDDING_MODEL="$EMBEDDING_MODEL" \
  --from-literal=AI_SERVICE_HOST="ai-service" \
  --from-literal=AI_SERVICE_PORT="8000" \
  --from-literal=BASE_PROBLEM_URI="http://${FRONTEND_HOST}/api/v1/inventory-service/problem" \
  --from-literal=SPRING_DATASOURCE_URL="jdbc:postgresql://postgres:5432/${POSTGRES_DB}" \
  --from-literal=SPRING_DATASOURCE_USERNAME="$POSTGRES_USER" \
  --from-literal=SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://${KEYCLOAK_HOST}/realms/${KEYCLOAK_REALM}" \
  --from-literal=SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI="http://keycloak:8080/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null

kubectl -n "$NAMESPACE" create configmap postgres-init \
  --from-file=db-init.sql="$ROOT/docs/architecture/db-init.sql" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null

FRONTEND_CONFIG="$(mktemp)"
REALM_CONFIG="$(mktemp)"
trap 'rm -f "$FRONTEND_CONFIG" "$REALM_CONFIG"' EXIT

cat >"$FRONTEND_CONFIG" <<EOF
{
  "apiUrl": "/api/v1/inventory-service",
  "keycloakUrl": "http://${KEYCLOAK_HOST}",
  "keycloakRealm": "${KEYCLOAK_REALM}",
  "keycloakClientId": "frontend-client"
}
EOF

kubectl -n "$NAMESPACE" create configmap frontend-config \
  --from-file=config.json="$FRONTEND_CONFIG" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null

# El realm de Compose apunta a localhost:4200; en Kubernetes el frontend se
# publica a través del Ingress.
sed "s|http://localhost:4200|http://${FRONTEND_HOST}|g" \
  "$ROOT/config/realm.json" >"$REALM_CONFIG"

kubectl -n "$NAMESPACE" create configmap keycloak-realm \
  --from-file=realm.json="$REALM_CONFIG" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null

kubectl -n "$NAMESPACE" create secret generic neuroinventory-secrets \
  --from-literal=POSTGRES_PASSWORD="$POSTGRES_PASSWORD" \
  --from-literal=DATABASE_URL="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@postgres:5432/${POSTGRES_DB}" \
  --from-literal=INTERNAL_API_KEY="$INTERNAL_API_KEY" \
  --from-literal=KC_BOOTSTRAP_ADMIN_USERNAME="$KC_BOOTSTRAP_ADMIN_USERNAME" \
  --from-literal=KC_BOOTSTRAP_ADMIN_PASSWORD="$KC_BOOTSTRAP_ADMIN_PASSWORD" \
  --from-literal=SERVICE_TOKEN="$SERVICE_TOKEN" \
  --dry-run=client -o yaml | kubectl apply -f - >/dev/null

NEW_APP_HASH="$(data_hash configmap app-config)"
NEW_FRONTEND_HASH="$(data_hash configmap frontend-config)"
NEW_SECRET_HASH="$(data_hash secret neuroinventory-secrets)"

BACKEND_CONFIG_CHANGED=0
if [[ -n "$OLD_APP_HASH" && "$OLD_APP_HASH" != "$NEW_APP_HASH" ]]; then
  BACKEND_CONFIG_CHANGED=1
fi
if [[ -n "$OLD_SECRET_HASH" && "$OLD_SECRET_HASH" != "$NEW_SECRET_HASH" ]]; then
  BACKEND_CONFIG_CHANGED=1
fi

FRONTEND_CONFIG_CHANGED=0
if [[ -n "$OLD_FRONTEND_HASH" && "$OLD_FRONTEND_HASH" != "$NEW_FRONTEND_HASH" ]]; then
  FRONTEND_CONFIG_CHANGED=1
fi

# -----------------------------------------------------------------------------
# Workloads
# -----------------------------------------------------------------------------
echo "==> Inicializando PostgreSQL"
kubectl apply -n "$NAMESPACE" -f "$ROOT/deploy/k8s/postgres.yaml"
kubectl -n "$NAMESPACE" rollout status statefulset/postgres --timeout=600s

# Job idempotente: crea las bases de datos y el esquema si aún no existen.
kubectl -n "$NAMESPACE" delete job postgres-init --ignore-not-found=true >/dev/null
kubectl apply -n "$NAMESPACE" -f "$ROOT/deploy/k8s/postgres-init-job.yaml"
kubectl -n "$NAMESPACE" wait --for=condition=complete job/postgres-init --timeout=300s

echo "==> Aplicando manifiestos"
# Los Jobs son inmutables: se recrean para volver a ejecutarlos.
kubectl -n "$NAMESPACE" delete job keycloak-config ollama-models \
  --ignore-not-found=true >/dev/null
# Los hosts del Ingress se sustituyen aquí porque Kustomize no puede leer
# variables de entorno.
kubectl kustomize "$ROOT/deploy/k8s" \
  | sed -e "s|__FRONTEND_HOST__|${FRONTEND_HOST}|g" \
        -e "s|__KEYCLOAK_HOST__|${KEYCLOAK_HOST}|g" \
  | kubectl apply -f -

# Keycloak e inventory-service leen los hosts de app-config al arrancar; un
# cambio en el ConfigMap no reinicia el pod por sí solo.
if [[ "$BACKEND_CONFIG_CHANGED" == "1" ]]; then
  echo "==> Configuración de backend cambiada: reiniciando servicios"
  kubectl -n "$NAMESPACE" rollout restart deployment/keycloak >/dev/null
  if [[ "$BUILD" != "1" ]]; then
    kubectl -n "$NAMESPACE" rollout restart \
      deployment/ai-service \
      deployment/mcp-server \
      deployment/inventory-service >/dev/null
  fi
fi

if [[ "$FRONTEND_CONFIG_CHANGED" == "1" && "$BUILD" != "1" ]]; then
  echo "==> Configuración del frontend cambiada: reiniciando frontend"
  kubectl -n "$NAMESPACE" rollout restart deployment/frontend >/dev/null
fi

# Las imágenes mantienen el tag entre builds: sin reiniciar, los pods seguirían
# ejecutando la versión anterior aunque el nodo ya tenga la nueva.
if [[ "$BUILD" == "1" ]]; then
  echo "==> Reiniciando los servicios con las imágenes nuevas"
  kubectl -n "$NAMESPACE" rollout restart \
    deployment/ai-service \
    deployment/mcp-server \
    deployment/inventory-service \
    deployment/frontend >/dev/null
fi

echo "==> Esperando a los servicios principales"
kubectl -n "$NAMESPACE" rollout status deployment/ollama --timeout=300s
kubectl -n "$NAMESPACE" rollout status deployment/keycloak --timeout=600s
kubectl -n "$NAMESPACE" wait --for=condition=complete job/keycloak-config --timeout=600s || \
  echo "AVISO: el Job de Keycloak aún no ha terminado; revisa 'kubectl -n $NAMESPACE logs job/keycloak-config'."
kubectl -n "$NAMESPACE" rollout status deployment/ai-service --timeout=300s
kubectl -n "$NAMESPACE" rollout status deployment/inventory-service --timeout=300s
kubectl -n "$NAMESPACE" rollout status deployment/mcp-server --timeout=300s
kubectl -n "$NAMESPACE" rollout status deployment/frontend --timeout=300s

MINIKUBE_IP="$(minikube ip)"

if [[ "${SKIP_HOSTS:-0}" == "1" ]]; then
  echo "==> SKIP_HOSTS=1: no se modifica /etc/hosts"
else
  echo "==> Configurando /etc/hosts"
  FRONTEND_HOST="$FRONTEND_HOST" KEYCLOAK_HOST="$KEYCLOAK_HOST" \
    "$ROOT/scripts/k8s-hosts.sh" add || \
    echo "AVISO: añade la entrada manualmente: ${MINIKUBE_IP} ${FRONTEND_HOST} ${KEYCLOAK_HOST}"
fi

cat <<EOF

===============================================================================
NeuroInventory desplegado en Kubernetes
===============================================================================
Entradas de /etc/hosts (se eliminan con ./scripts/k8s-down.sh):

  ${MINIKUBE_IP} ${FRONTEND_HOST} ${KEYCLOAK_HOST}

URLs:
  Frontend:   http://${FRONTEND_HOST}
  Keycloak:   http://${KEYCLOAK_HOST}
  API:        http://${FRONTEND_HOST}/api/v1/inventory-service

La descarga de modelos de Ollama sigue en segundo plano. Comprueba su estado:

  kubectl -n ${NAMESPACE} get job ollama-models
  kubectl -n ${NAMESPACE} logs job/ollama-models -f

Pods:

  kubectl -n ${NAMESPACE} get pods

===============================================================================
EOF

kubectl -n "$NAMESPACE" get pods
