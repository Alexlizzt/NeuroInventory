"""Configuración de observabilidad para el AI Service.

Configura:
- Trazas distribuidas con OpenTelemetry exportadas por OTLP al colector.
- Métricas Prometheus expuestas en el endpoint /metrics.
"""

import os
import logging

from opentelemetry import trace
from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
from opentelemetry.sdk.resources import Resource
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor

from prometheus_client import Counter, Histogram, generate_latest, CONTENT_TYPE_LATEST

logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Métricas Prometheus (definidas a nivel de módulo para reutilización)
# ---------------------------------------------------------------------------

RAG_QUERIES_TOTAL = Counter(
    "ai_service_rag_queries_total",
    "Número total de consultas RAG procesadas",
    ["status"],
)

SEMANTIC_SEARCH_TOTAL = Counter(
    "ai_service_semantic_search_total",
    "Número total de búsquedas semánticas realizadas",
    ["status"],
)

EMBEDDING_GENERATION_TOTAL = Counter(
    "ai_service_embedding_generation_total",
    "Número total de embeddings generados",
    ["status"],
)

HTTP_REQUEST_DURATION = Histogram(
    "ai_service_http_request_duration_seconds",
    "Duración de las peticiones HTTP al AI Service",
    ["method", "endpoint", "status"],
)


def configure_otel(app) -> None:
    """Inicializa OpenTelemetry y instrumenta la aplicación FastAPI."""
    otlp_endpoint = os.getenv(
        "OTEL_EXPORTER_OTLP_TRACES_ENDPOINT",
        "http://otel-collector:4318",
    )
    service_name = os.getenv("OTEL_SERVICE_NAME", "ai-service")

    resource = Resource(attributes={"service.name": service_name})

    provider = TracerProvider(resource=resource)
    processor = BatchSpanProcessor(
        OTLPSpanExporter(endpoint=otlp_endpoint)
    )
    provider.add_span_processor(processor)
    trace.set_tracer_provider(provider)

    # Instrumentar FastAPI automáticamente (captura requests HTTP)
    FastAPIInstrumentor.instrument_app(app, tracer_provider=provider)

    logger.info(
        "OpenTelemetry configurado para %s -> %s", service_name, otlp_endpoint
    )


def metrics_response():
    """Devuelve las métricas en formato Prometheus."""
    from fastapi import Response
    return Response(
        content=generate_latest(),
        media_type=CONTENT_TYPE_LATEST,
    )
