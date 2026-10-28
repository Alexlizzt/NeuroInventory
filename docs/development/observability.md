# Observabilidad — NeuroInventory

Esta guía explica cómo levantar y usar la pila de observabilidad de NeuroInventory:
**OpenTelemetry (trazas) + Prometheus (métricas) + Grafana (dashboards)**.

Se implementa como un archivo de Docker Compose separado para no afectar el arranque básico del proyecto.

---

## Arquitectura de observabilidad

```text
instrumented services
   │  OTLP (traces + metrics)
   ▼
otel-collector ──────┬──► Prometheus (métricas)
   │                  │
   │                  └──► Grafana (dashboards + datasources)
   │
   └──► logging (consola del colector, para depuración)
```

Servicios instrumentados:
| Servicio | Métricas | Trazas | Endpoint |
|----------|----------|--------|----------|
| inventory-service | `/actuator/prometheus` | OTLP | `:8080` |
| ai-service | `/metrics` | OTLP | `:8000` |
| frontend (Angular) | — | OTLP (navegador) | `:4200` |
| mcp-server | — | OTLP | `:8001` |
| otel-collector | `:8888` (Prometheus) | — | `:8888` |

---

## 1. Levantar la pila completa

Desde la raíz del proyecto:

```bash
# Levantar servicios base + observabilidad
docker compose -f docker-compose.yml -f docker-compose.observability.yml up --build
```

Esto levantará:
- Todos los servicios base (postgres, keycloak, ollama, ai-service, inventory-service, mcp-server, frontend)
- `otel-collector` en puertos `4317` (gRPC), `4318` (HTTP), `8888` (Prometheus)
- `prometheus` en puerto `9091` (el `9090` lo usa Keycloak)
- `grafana` en puerto `3000`

> Si ya tenías los servicios base levantados, solo levanta observabilidad:
> ```bash
> docker compose -f docker-compose.observability.yml up --build
> ```

---

## 2. URLs de acceso

| Servicio | URL | Usuario / Contraseña |
|----------|-----|----------------------|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9091 | — |
| OTLP Collector (traces) | http://localhost:4318 | — |
| OTLP Collector (metrics) | http://localhost:8888 | — |

---

## 3. Configuración por servicio

### Variables de entorno relevantes

Definidas en `.env.example`:

```env
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=http://otel-collector:4318
OTEL_SERVICE_NAME=neuroinventory
```

Estas variables se inyectan automáticamente en `inventory-service`, `ai-service` y `mcp-server` mediante `docker-compose.observability.yml`.

### inventory-service (Spring Boot)

- Métricas expuestas en: `http://localhost:8080/api/v1/inventory-service/actuator/prometheus`
- Endpoints Actuator expuestos: `health`, `info`, `metrics`, `prometheus`
- Trazas automáticas de peticiones HTTP (Servlet) y llamadas a base de datos (JPA/Hibernate) mediante OpenTelemetry Spring Boot Starter.

### ai-service (FastAPI)

- Métricas expuestas en: `http://localhost:8000/metrics`
- Métricas personalizadas:
  - `ai_service_rag_queries_total` — consultas RAG (por estado)
  - `ai_service_semantic_search_total` — búsquedas semánticas (por estado)
  - `ai_service_embedding_generation_total` — generación de embeddings (por estado)
  - `ai_service_http_request_duration_seconds` — duración de peticiones HTTP
- Trazas automáticas de peticiones FastAPI mediante `FastAPIInstrumentor`. El
  exportador OTLP solo se activa si `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT` está
  definido; sin esa variable (por ejemplo en Kubernetes sin colector) el servicio
  no intenta enviar trazas y evita errores de conexión en los logs.

### frontend (Angular)

- Trazas de navegación (DocumentLoad) y peticiones XHR/HttpClient enviadas al colector OTLP.
- Inicializado en `frontend/src/main.ts` mediante `initTracing()`.

### mcp-server

- Trazas OTLP enviadas al colector (configurado mediante variable de entorno).

---

## 4. Dashboards de Grafana

Al arrancar Grafana con el archivo de observabilidad, se provee automáticamente:

- **Datasource Prometheus** (predeterminado): `http://prometheus:9090`
- **Dashboard**: `NeuroInventory - Observabilidad` con paneles de:
  - HTTP Requests/s (Inventory y AI Service)
  - Duración p95 de peticiones HTTP
  - Tasa de errores 4xx / 5xx
  - Uso de memoria JVM
  - Consultas RAG y búsquedas semánticas (AI Service)

Credenciales por defecto: `admin` / `admin`.

---

## 5. Cómo verificar que funciona

### Prometheus está recolectando métricas

```bash
# inventory-service
curl -s http://localhost:8080/api/v1/inventory-service/actuator/prometheus | head -20

# ai-service
curl -s http://localhost:8000/metrics | head -20

# Prometheus targets
curl -s http://localhost:9091/api/v1/targets
```

### Trazas están llegando al colector

Revisa los logs del contenedor `otel-collector`:

```bash
docker compose -f docker-compose.observability.yml logs otel-collector
```

Deberías ver spans exportados con `logging` exporter.

### Grafana

1. Abre http://localhost:3000
2. Inicia sesión con `admin` / `admin`
3. El dashboard `NeuroInventory - Observabilidad` debería aparecer con datos después de generar algo de tráfico en la plataforma.

---

## 6. Alertas de Prometheus

Las reglas de alerta están definidas en `observability/prometheus/alerts.yml` y se cargan automáticamente al iniciar Prometheus.

### Alertas configuradas

| Alerta | Condición | Severidad |
|--------|-----------|-----------|
| `ServiceDown` | `up == 0` durante 30s | critical |
| `HighErrorRateInventory` | Errores 5xx > 5% en 2m | warning |
| `HighErrorRateAIService` | Errores 5xx > 5% en 2m | warning |
| `HighLatencyInventory` | p95 > 2s en 3m | warning |
| `HighLatencyAIService` | p95 > 3s en 3m | warning |
| `HighJvmMemoryUsage` | Heap > 85% en 5m | warning |
| `SemanticSearchFailures` | Búsquedas semánticas fallando en 5m | warning |
| `RagQueryFailures` | Consultas RAG fallando en 5m | warning |
| `EmbeddingGenerationFailures` | Embeddings fallando en 5m | warning |
| `PrometheusTargetMissing` | Target no scrapeado en 1m | critical |

### Ver reglas activas

Desde la UI de Prometheus:

```
http://localhost:9091/alerts
```

O mediante API:

```bash
curl -s http://localhost:9091/api/v1/rules | python3 -m json.tool
```

### Alertmanager

Alertmanager está incluido en `docker-compose.observability.yml` y expuesto en:

```
http://localhost:9093
```

Por defecto está configurado para recibir alertas pero **sin notificaciones externas** (solo se registran en los logs del contenedor). Para activar notificaciones reales, edita `observability/prometheus/alertmanager.yml` y configura un receptor (email, Slack, PagerDuty, etc.).

### Silenciar una alerta temporalmente

Desde la UI de Alertmanager (`http://localhost:9093`) puedes crear un "silence" para pausar notificaciones de una alerta específica durante un período determinado.

---

## 7. Detener la observabilidad

```bash
docker compose -f docker-compose.observability.yml down
```

Para eliminar también los volúmenes de Prometheus y Grafana:

```bash
docker compose -f docker-compose.observability.yml down -v
```

---

## 7. Próximas mejoras sugeridas

| Mejora | Descripción |
|--------|-------------|
| Jaeger / Tempo | Almacenamiento y consulta de trazas persistentes (actualmente solo logging en el colector) |
| Alertas en Prometheus | Reglas de alerta para error rate alto, latencia p95 > umbral, servicio caído |
| Métricas de negocio | Contadores de productos creados, movimientos de stock, sesiones de chat IA |
| Traces en frontend | Correlacionar `traceId` entre frontend y backend para trazas end-to-end completas |
| Logs estructurados | Enviar logs de todos los servicios a Loki o Elasticsearch |
