# Observabilidad

## Estado actual

| Capacidad | Implementación |
| --- | --- |
| Health checks | Spring Boot Actuator (`inventory-service`), `/health` (`ai-service`), healthchecks de Docker en todos los servicios |
| Logs | Salida estándar en JSON/texto por contenedor (`docker compose logs`) |
| Métricas | Spring Boot Actuator (`/actuator/metrics`) |
| Trazas | No implementado |

## Logs estructurados

- `inventory-service`: logs de Spring Boot con nivel configurable.
- `ai-service`: logs de uvicorn + logging de Python.
- Consulta centralizada con:

```bash
docker compose logs -f <servicio>
```

## Health checks

Cada servicio define un `HEALTHCHECK` o healthcheck en `docker-compose.yml`:

- `postgres`: `pg_isready`.
- `keycloak`: `/health/live`.
- `ai-service`: `curl -f http://localhost:8000/health`.
- Dependencias entre servicios gestionadas con `depends_on: condition: service_healthy`.

## Roadmap

- **OpenTelemetry** para trazas distribuidas entre `frontend → inventory-service → ai-service → postgres/ollama`.
- **Prometheus** para recolección de métricas.
- **Grafana** para dashboards.
- Métricas de negocio: latencia de búsqueda semántica, tasa de errores RFC 9457, uso del LLM.
