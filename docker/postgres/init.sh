#!/bin/bash
set -e

echo "Creating Keycloak database..."

KEYCLOAK_DB="${KEYCLOAK_DB:-keycloak}"

psql -v ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "postgres" \
    -c "CREATE DATABASE \"$KEYCLOAK_DB\";"

echo "Keycloak database ready."

echo "Initializing NeuroInventory database..."

psql -v ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    -f /scripts/db-init.sql

echo "NeuroInventory database ready."
