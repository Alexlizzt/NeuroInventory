from fastapi import FastAPI
from app.api.routes import router as ai_router

app = FastAPI(
    title="NeuroInventory - AI Service",
    version="1.0.0",
    description="Servicio de IA para Embeddings, Búsqueda Semántica y RAG"
)

app.include_router(ai_router, prefix="/api/v1")

@app.get("/health")
def health_check():
    return {"status": "ok"}