from functools import lru_cache

from fastapi import APIRouter, HTTPException, status, Depends
from pydantic import BaseModel, ConfigDict, Field

from app.security.auth import verify_api_key
from app.search.semantic import SemanticSearchService

# Router para capacidades de búsqueda semántica (protegido con API Key interna)
router = APIRouter(
    prefix="/search",
    tags=["Semantic Search"],
    dependencies=[Depends(verify_api_key)]
)


@lru_cache(maxsize=1)
def get_search_service() -> SemanticSearchService:
    """Construye el servicio de forma perezosa (evita conectar a la BD al importar)."""
    return SemanticSearchService()

# ==============================================================================
# 1. SCHEMAS / DTOs
# ==============================================================================

class IndexProductRequest(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    product_id: str = Field(alias="productId", description="ID del producto a indexar")
    name: str | None = Field(default=None, description="Nombre del producto")
    sku: str | None = Field(default=None, description="SKU del producto")
    description: str | None = Field(default=None, description="Descripción del producto")
    metadata: dict = Field(default_factory=dict, description="Metadatos adicionales")


class IndexProductResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    status: str = Field(description="Resultado de la operación", examples=["SUCCESS"])
    product_id: str = Field(alias="productId", description="ID del producto indexado")


class ProductMatch(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    product_id: str = Field(alias="productId")
    score: float = Field(description="Similitud coseno entre 0 y 1")
    rationale: str = Field(description="Explicación legible de la coincidencia")


class SemanticSearchRequest(BaseModel):
    query: str = Field(..., description="Consulta en lenguaje natural", examples=["laptop liviana para programar"])
    limit: int = Field(default=5, ge=1, le=50, description="Número máximo de coincidencias")


class SemanticSearchResponse(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    matches: list[ProductMatch] = Field(default_factory=list)
    interpreted_query: str = Field(alias="interpretedQuery", description="Consulta normalizada utilizada")

# ==============================================================================
# 2. ENDPOINTS
# ==============================================================================

@router.post(
    "/index",
    response_model=IndexProductResponse,
    summary="Indexar producto",
    description="Genera el embedding de un producto y lo almacena/actualiza en pgvector.",
)
async def index_product(payload: IndexProductRequest, service: SemanticSearchService = Depends(get_search_service)):
    try:
        indexed = service.index_product(
            product_id=payload.product_id,
            name=payload.name,
            sku=payload.sku,
            description=payload.description,
            metadata=payload.metadata,
        )
        if not indexed:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="El producto no contiene texto indexable (name/sku/description).",
            )
        return IndexProductResponse(status="SUCCESS", productId=payload.product_id)
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error indexando producto: {str(e)}",
        )


@router.delete(
    "/index/{product_id}",
    status_code=status.HTTP_204_NO_CONTENT,
    summary="Eliminar índice de producto",
    description="Elimina el embedding asociado a un producto.",
)
async def delete_product_index(product_id: str, service: SemanticSearchService = Depends(get_search_service)):
    try:
        service.delete_product(product_id)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error eliminando índice del producto: {str(e)}",
        )


@router.post(
    "/semantic",
    response_model=SemanticSearchResponse,
    summary="Búsqueda semántica de productos",
    description="Recupera los productos más similares a una consulta usando similitud vectorial en pgvector.",
)
async def semantic_search(payload: SemanticSearchRequest, service: SemanticSearchService = Depends(get_search_service)):
    try:
        matches = service.search(query=payload.query, limit=payload.limit)
        return SemanticSearchResponse(matches=matches, interpretedQuery=payload.query)
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error procesando la búsqueda semántica: {str(e)}",
        )
