from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel, Field

from app.security.auth import verify_api_key
from app.embeddings.generator import EmbeddingService
from app.rag.pipeline import RAGService

# Aplicamos la protección con API Key a todas las rutas declaradas en este router
router = APIRouter(
    prefix="/api/v1/rag",
    tags=["RAG"],
    dependencies=[Depends(verify_api_key)]
)

# Instancias de servicio
embedding_service = EmbeddingService()
rag_service = RAGService()

# ==============================================================================
# 1. SCHEMAS / DTOs
# ==============================================================================

class EmbeddingRequest(BaseModel):
    text: str = Field(..., description="Texto fuente para generar el vector de embeddings", example="Mantenimiento preventivo de motor")

class EmbeddingResponse(BaseModel):
    embedding: list[float] = Field(..., description="Lista de valores flotantes del vector embedding")

class DocumentIngestRequest(BaseModel):
    product_id: str = Field(..., description="ID del producto al que pertenece el documento", example="PROD-10293")
    content: str = Field(..., description="Contenido en texto plano del manual o documentación")
    metadata: dict = Field(default_factory=dict, description="Metadatos adicionales para filtrado")

class DocumentIngestResponse(BaseModel):
    status: str = Field(..., example="SUCCESS")
    doc_id: str = Field(..., description="Identificador único del documento ingestado")
    chunks_created: int = Field(..., description="Cantidad de fragmentos procesados")

class RAGQueryRequest(BaseModel):
    question: str = Field(..., description="Consulta en lenguaje natural", example="¿Cómo se limpia el filtro?")
    product_id: str | None = Field(default=None, description="Opcional: ID de producto para limitar el contexto")

class RAGQueryResponse(BaseModel):
    answer: str = Field(..., description="Respuesta contextualizada por el LLM")
    sources: list[str] = Field(..., description="Fragmentos de texto recuperados como fuente")

class ErrorDetail(BaseModel):
    detail: str

COMMON_RESPONSES = {
    status.HTTP_403_FORBIDDEN: {
        "model": ErrorDetail,
        "description": "API Key inválida o ausente en la cabecera X-API-KEY"
    },
    status.HTTP_500_INTERNAL_SERVER_ERROR: {
        "model": ErrorDetail,
        "description": "Error interno del servidor o falla en la comunicación con Ollama/PGVector"
    }
}

# ==============================================================================
# 2. ENDPOINTS
# ==============================================================================

@router.post(
    "/embeddings", 
    response_model=EmbeddingResponse,
    responses=COMMON_RESPONSES,
    summary="Generar Vector Embedding",
    description="Calcula la representación vectorial de un texto usando el modelo de embeddings configurado."
)
async def generate_embedding(payload: EmbeddingRequest):
    try:
        vector = embedding_service.generate_embedding(payload.text)
        return EmbeddingResponse(embedding=vector)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
            detail=f"Error generando embedding: {str(e)}"
        )

@router.post(
    "/documents", 
    response_model=DocumentIngestResponse,
    responses=COMMON_RESPONSES,
    summary="Ingestar Documentación de Producto",
    description="Divide en fragmentos e ingesta un manual en la base de datos de vectores PGVector."
)
async def ingest_document(payload: DocumentIngestRequest):
    try:
        doc_id, chunks = rag_service.ingest_document(
            product_id=payload.product_id,
            content=payload.content,
            extra_meta=payload.metadata
        )
        return DocumentIngestResponse(
            status="SUCCESS",
            doc_id=doc_id,
            chunks_created=chunks
        )
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
            detail=f"Error al ingestar documento: {str(e)}"
        )

@router.post(
    "/query", 
    response_model=RAGQueryResponse,
    responses=COMMON_RESPONSES,
    summary="Consultar Motor RAG",
    description="Recupera contexto vectorial de PGVector y responde preguntas usando la LLM local."
)
async def query_rag(payload: RAGQueryRequest):
    try:
        answer, sources = rag_service.query(
            question=payload.question,
            product_id=payload.product_id
        )
        return RAGQueryResponse(answer=answer, sources=sources)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, 
            detail=f"Error procesando la consulta RAG: {str(e)}"
        )