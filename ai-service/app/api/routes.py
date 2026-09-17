from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

from app.embeddings.generator import EmbeddingService
from app.rag.pipeline import RAGService

router = APIRouter()

# Instancia de servicios (o puedes usar Depends si los registras como dependencias)
embedding_service = EmbeddingService()
rag_service = RAGService()

# ==============================================================================
# 1. SCHEMAS / DTOs
# ==============================================================================

class EmbeddingRequest(BaseModel):
    text: str

class EmbeddingResponse(BaseModel):
    embedding: list[float]

class DocumentIngestRequest(BaseModel):
    product_id: str
    content: str
    metadata: dict = Field(default_factory=dict)

class DocumentIngestResponse(BaseModel):
    status: str
    doc_id: str
    chunks_created: int

class RAGQueryRequest(BaseModel):
    question: str
    product_id: str | None = None

class RAGQueryResponse(BaseModel):
    answer: str
    sources: list[str]

class ErrorDetail(BaseModel):
    detail: str

ERROR_500_RESPONSE = {
    status.HTTP_500_INTERNAL_SERVER_ERROR: {
        "model": ErrorDetail,
        "description": "Error interno del servidor o falla en la comunicación con Ollama/Database"
    }
}

# ==============================================================================
# 2. ENDPOINTS
# ==============================================================================

@router.post(
    "/embeddings/generate", 
    response_model=EmbeddingResponse,
    responses=ERROR_500_RESPONSE
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
    "/rag/documents", 
    response_model=DocumentIngestResponse,
    responses=ERROR_500_RESPONSE
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
    "/rag/query", 
    response_model=RAGQueryResponse,
    responses=ERROR_500_RESPONSE
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