#!/usr/bin/env bash
# =============================================================================
# Elimina los workloads de NeuroInventory de minikube.
#
#   ./scripts/k8s-down.sh           # conserva los PVC (PostgreSQL y modelos)
#   ./scripts/k8s-down.sh --purge   # elimina también los PVC y el namespace
# =============================================================================
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
NAMESPACE="${NAMESPACE:-neuroinventory}"

if ! kubectl get namespace "$NAMESPACE" >/dev/null 2>&1; then
  echo "El namespace '$NAMESPACE' no existe."
  exit 0
fi

if [[ "${1:-}" == "--purge" ]]; then
  echo "==> Eliminando todos los recursos (incluidos los datos)"
  kubectl delete -k "$ROOT/deploy/k8s" --ignore-not-found=true
else
  echo "==> Eliminando workloads (los PVC se conservan)"
  kubectl -n "$NAMESPACE" delete \
    deployment,statefulset,service,ingress,job \
    --all --ignore-not-found=true
  # Solo la configuración propia: los ConfigMaps del sistema (kube-root-ca.crt)
  # se gestionan solos y no deben tocarse.
  kubectl -n "$NAMESPACE" delete \
    configmap/app-config configmap/frontend-config \
    configmap/postgres-init configmap/keycloak-realm \
    secret/neuroinventory-secrets \
    --ignore-not-found=true
fi
