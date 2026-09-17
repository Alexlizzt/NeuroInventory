from llama_index.core import (
    VectorStoreIndex, 
    Document, 
    Settings as LlamaSettings
)
from llama_index.core.node_parser import SentenceSplitter
from llama_index.core.vector_stores import (
    MetadataFilters, 
    ExactMatchFilter
)
from llama_index.llms.ollama import Ollama
from llama_index.embeddings.ollama import OllamaEmbedding
from llama_index.vector_stores.postgres import PGVectorStore

from app.config import settings


class RAGService:
    def __init__(self):
        # 1. Configuración global de LlamaIndex
        LlamaSettings.llm = Ollama(
            model=settings.llm_model, 
            base_url=settings.ollama_host,
            request_timeout=180.0
        )
        LlamaSettings.embed_model = OllamaEmbedding(
            model_name=settings.embedding_model,
            base_url=settings.ollama_host
        )
        
        # 2. Conexión al almacén vectorial PGVector
        self.vector_store = PGVectorStore.from_params(
            database=settings.postgres_db,
            host=settings.postgres_host,
            password=settings.postgres_password,
            port=settings.postgres_port,
            user=settings.postgres_user,
            table_name="product_documents_vectors",
            embed_dim=getattr(settings, "EMBED_DIM", 768),
        )

    def ingest_document(self, product_id: str, content: str, extra_meta: dict) -> tuple[str, int]:
        """Fragmenta e ingesta la documentación técnica de un producto."""
        meta = {"product_id": product_id, **extra_meta}
        doc = Document(text=content, metadata=meta)
        
        parser = SentenceSplitter(chunk_size=512, chunk_overlap=50)
        nodes = parser.get_nodes_from_documents([doc])

        index = VectorStoreIndex.from_vector_store(vector_store=self.vector_store)
        index.insert_nodes(nodes)
        
        return doc.doc_id, len(nodes)

    def query(self, question: str, product_id: str | None = None) -> tuple[str, list[str]]:
        """Recupera contexto filtrado por producto y responde mediante el LLM."""
        index = VectorStoreIndex.from_vector_store(vector_store=self.vector_store)
        
        # Construcción dinámica de filtros de metadatos
        filters = None
        if product_id:
            filters = MetadataFilters(
                filters=[
                    ExactMatchFilter(key="product_id", value=product_id)
                ]
            )

        query_engine = index.as_query_engine(
            similarity_top_k=3,
            filters=filters
        )
        
        response = query_engine.query(question)

        sources = [
            node.node.get_content()[:100] + "..." 
            for node in response.source_nodes
        ]
        return str(response), sources