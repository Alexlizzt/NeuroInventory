#!/bin/bash
set -e

echo "Checking Keycloak database..."

KEYCLOAK_DB="${KEYCLOAK_DB:-keycloak}"

# Verificar si la base de datos ya existe antes de crearla
DB_EXISTS=$(psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" -tAc "SELECT 1 FROM pg_database WHERE datname='$KEYCLOAK_DB'")

if [ "$DB_EXISTS" = "1" ]; then
    echo "Keycloak database already exists."
else
    echo "Creating Keycloak database..."
    psql -v ON_ERROR_STOP=1 \
        --username "$POSTGRES_USER" \
        --dbname "postgres" \
        -c "CREATE DATABASE \"$KEYCLOAK_DB\";"
    echo "Keycloak database created successfully."
fi

echo "Initializing NeuroInventory database..."

psql -v ON_ERROR_STOP=1 \
    --username "$POSTGRES_USER" \
    --dbname "$POSTGRES_DB" \
    -f /scripts/db-init.sql

echo "NeuroInventory database ready."