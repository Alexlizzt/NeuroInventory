#!/usr/bin/env bash
# =============================================================================
# Gestiona las entradas de /etc/hosts para acceder a NeuroInventory en minikube.
#
#   ./scripts/k8s-hosts.sh add      # añade/actualiza la entrada con la IP actual
#   ./scripts/k8s-hosts.sh remove   # elimina las entradas
#   ./scripts/k8s-hosts.sh status   # muestra el estado actual
#
# Variables opcionales:
#   FRONTEND_HOST=neuroinventory.local
#   KEYCLOAK_HOST=auth.neuroinventory.local
#   HOSTS_FILE=/etc/hosts   (útil para pruebas sin sudo)
# =============================================================================
set -euo pipefail

FRONTEND_HOST="${FRONTEND_HOST:-neuroinventory.local}"
KEYCLOAK_HOST="${KEYCLOAK_HOST:-auth.neuroinventory.local}"
HOSTS_FILE="${HOSTS_FILE:-/etc/hosts}"
# Imagen mínima usada para escribir /etc/hosts como root a través de Docker.
HOSTS_IMAGE="${HOSTS_IMAGE:-busybox:1.36}"

# Escribe el fichero preparado en $1 conservando inodo, permisos y propietario.
write_hosts() {
  local tmp="$1"

  if [[ "$HOSTS_FILE" != "/etc/hosts" ]]; then
    tee "$HOSTS_FILE" <"$tmp"
    return 0
  fi

  # El daemon de Docker corre como root: no hace falta la contraseña de sudo.
  # Se monta solo el fichero, no el directorio /etc completo.
  if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    if docker run --rm -i -v "$HOSTS_FILE:/hosts" "$HOSTS_IMAGE" \
      sh -c 'cat > /hosts' <"$tmp"; then
      return 0
    fi
  fi

  sudo tee "$HOSTS_FILE" <"$tmp" >/dev/null
}

# Elimina las líneas que resuelven nuestros hosts, ignorando comentarios.
filter_entries() {
  awk -v h1="$FRONTEND_HOST" -v h2="$KEYCLOAK_HOST" '
    {
      line = $0
      sub(/#.*/, "", line)
      n = split(line, fields, /[[:space:]]+/)
      for (i = 2; i <= n; i++) {
        if (fields[i] == h1 || fields[i] == h2) next
      }
      print $0
    }' "$HOSTS_FILE"
}

minikube_ip() {
  minikube ip 2>/dev/null
}

add_entries() {
  local ip entry tmp

  if ! ip="$(minikube_ip)"; then
    echo "ERROR: minikube no está arrancado. Ejecuta 'minikube start'." >&2
    return 1
  fi

  entry="$ip $FRONTEND_HOST $KEYCLOAK_HOST"

  if grep -qxF -- "$entry" "$HOSTS_FILE" 2>/dev/null; then
    echo "==> $HOSTS_FILE ya contiene: $entry"
    return 0
  fi

  tmp="$(mktemp)"
  filter_entries >"$tmp"
  printf '%s\n' "$entry" >>"$tmp"

  if ! write_hosts "$tmp"; then
    rm -f "$tmp"
    echo "AVISO: no se pudo escribir en $HOSTS_FILE. Añade la entrada manualmente:" >&2
    echo "  $entry" >&2
    return 1
  fi

  rm -f "$tmp"
  echo "==> Añadido a $HOSTS_FILE: $entry"
}

remove_entries() {
  local tmp

  tmp="$(mktemp)"
  filter_entries >"$tmp"

  if cmp -s "$tmp" "$HOSTS_FILE"; then
    rm -f "$tmp"
    echo "==> $HOSTS_FILE no contiene entradas de NeuroInventory"
    return 0
  fi

  if ! write_hosts "$tmp"; then
    rm -f "$tmp"
    echo "AVISO: no se pudieron eliminar las entradas de $HOSTS_FILE." >&2
    return 1
  fi

  rm -f "$tmp"
  echo "==> Entradas de NeuroInventory eliminadas de $HOSTS_FILE"
}

show_status() {
  local ip

  ip="$(minikube_ip || true)"
  echo "IP de minikube: ${ip:-desconocida}"
  echo "Entradas en $HOSTS_FILE:"
  grep -nE "(^|[[:space:]])(${FRONTEND_HOST}|${KEYCLOAK_HOST})([[:space:]]|$)" "$HOSTS_FILE" \
    || echo "  (ninguna)"
}

case "${1:-}" in
  add) add_entries ;;
  remove) remove_entries ;;
  status) show_status ;;
  *)
    echo "Uso: $(basename "$0") {add|remove|status}" >&2
    exit 1
    ;;
esac
