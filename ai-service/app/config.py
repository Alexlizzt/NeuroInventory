import os

class Settings:
    ollama_host: str = os.getenv("OLLAMA_HOST", "http://localhost:11434")
    llm_model: str = os.getenv("LLM_MODEL", "llama3.2")
    embedding_model: str = os.getenv("EMBEDDING_MODEL", "nomic-embed-text")
    database_url: str = os.getenv("DATABASE_URL", "")
    postgres_db: str = os.getenv("POSTGRES_DB", "postgres")
    postgres_host: str = os.getenv("POSTGRES_HOST", "localhost")
    postgres_password: str = os.getenv("POSTGRES_PASSWORD", "postgres")
    postgres_port: int = int(os.getenv("POSTGRES_PORT", "5432"))
    postgres_user: str = os.getenv("POSTGRES_USER", "postgres")

settings = Settings()
