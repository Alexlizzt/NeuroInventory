from fastapi import FastAPI
from app.api.routes import router as ai_router
from app.api.search_routes import router as search_router
from app.observability import configure_otel, metrics_response

app = FastAPI(
    title="NeuroInventory - AI Service",
    version="1.0.0",
    description="Servicio de IA para Embeddings, Búsqueda Semántica y RAG"
)

# Configurar observabilidad (OpenTelemetry + instrumentación FastAPI)
configure_otel(app)

app.include_router(ai_router, prefix="/api/v1")
app.include_router(search_router, prefix="/api/v1")


@app.get("/health")
def health_check():
    return {"status": "ok"}


@app.get("/metrics", include_in_schema=False)
def metrics():
    """Endpoint de métricas para Prometheus."""
    return metrics_response()