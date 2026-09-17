import os
from typing import Generator
from langchain_community.embeddings import OllamaEmbeddings
from langchain_community.llms import Ollama
from langchain_postgres import PGVector
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, Session
from settings import settings

# 1. Variables de entorno / Configuración
DATABASE_URL = settings.database_url
OLLAMA_HOST = settings.ollama_host

# Engine y Sessionmaker de SQLAlchemy
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)


# 2. Dependencia para la base de datos relacional
def get_db() -> Generator[Session, None, None]:
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


# 3. Dependencia para Embeddings (Ollama)
def get_embeddings() -> OllamaEmbeddings:
    return OllamaEmbeddings(
        base_url=OLLAMA_HOST,
        model="nomic-embed-text"  # o el modelo de embeddings que tengas en Ollama
    )


# 4. Dependencia para el Vector Store (PGVector)
def get_vector_store() -> PGVector:
    embeddings = get_embeddings()
    return PGVector(
        embeddings=embeddings,
        collection_name="inventory_documents",
        connection=DATABASE_URL,
        use_jsonb=True,
    )


# 5. Dependencia para el LLM (Ollama)
def get_llm() -> Ollama:
    return Ollama(
        base_url=OLLAMA_HOST,
        model=settings.llm_model,
        temperature=0.2
    )