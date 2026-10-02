# Inventory Service — NeuroInventory

Servicio principal del dominio construido con **Java 21** y **Spring Boot 4**.

Responsabilidades:

- Productos, categorías, stock y movimientos de inventario.
- Seguridad (OAuth2 Resource Server con Keycloak).
- APIs REST.

## Prerrequisitos

- Java 21.
- PostgreSQL en ejecución.
- Keycloak en ejecución (para validación de JWT).
- AI Service en ejecución (para funcionalidades de IA).

## Configuración

Variables de entorno (ver `docker-compose.yml`):

| Variable | Descripción |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Credenciales |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` | Issuer URI de Keycloak |
| `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI` | URI del JWK Set |
| `AI_SERVICE_HOST` / `AI_SERVICE_PORT` | Ubicación del AI Service |
| `INTERNAL_API_KEY` | Clave para llamadas internas |
| `BASE_PROBLEM_URI` | URI base para errores RFC 9457 |

## Build y ejecución

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080`.

Para compilar sin ejecutar:

```bash
./gradlew build
```

## Tests

```bash
./gradlew test
```

Los tests de integración usan Testcontainers (requieren Docker en ejecución).

## Docker

```bash
docker build -t neuroinventory-inventory-service .
docker run -p 8080:8080 --env-file ../.env neuroinventory-inventory-service
```

O desde la raíz del repo:

```bash
docker compose up inventory-service
```

## Estructura

El proyecto sigue Clean Architecture:

```text
src/main/java/com/alexlizzt/inventory_service/
├── domain/          # modelo, excepciones, puertos y servicios de dominio
├── application/     # casos de uso, comandos, queries, DTOs y mappers
└── infraestructure/ # web, persistence, security, ai, configuration
```
