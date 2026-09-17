from llama_index.embeddings.ollama import OllamaEmbedding
from app.config import settings

class EmbeddingService:
    def __init__(self):
        self.embed_model = OllamaEmbedding(
            model_name=settings.embedding_model,
            base_url=settings.ollama_host,
        )

    def generate_embedding(self, text: str) -> list[float]:
        """Genera el vector de embedding para una cadena de texto (ej. descripción de producto)."""
        return self.embed_model.get_text_embedding(text)