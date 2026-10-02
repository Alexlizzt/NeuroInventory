# AI Service — NeuroInventory

Microservicio de inteligencia artificial construido con **Python 3.12** y **FastAPI**.

Responsabilidades:

- Generación de embeddings.
- Búsqueda vectorial.
- Pipeline RAG.
- Integración con LLM (Ollama).

## Prerrequisitos

- Python 3.12.
- PostgreSQL con pgvector en ejecución.
- Ollama en ejecución (`OLLAMA_HOST`).
- Variables de entorno definidas (ver sección de configuración).

## Configuración

Variables de entorno (definidas en el `.env` raíz del repo y consumidas por `docker-compose.yml`):

| Variable | Descripción |
| --- | --- |
| `DATABASE_URL` | URL de conexión a PostgreSQL |
| `POSTGRES_USER` / `POSTGRES_PASSWORD` / `POSTGRES_DB` | Credenciales de la base de datos |
| `POSTGRES_HOST` / `POSTGRES_PORT` | Host y puerto de PostgreSQL |
| `OLLAMA_HOST` | URL del servidor Ollama |
| `INTERNAL_API_KEY` | Clave para comunicación interna con inventory-service |

## Instalación

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
pip install -r requirements-dev.txt  # para desarrollo/tests
```

## Servidor de desarrollo

```bash
uvicorn main:app --reload --port 8000
```

La API queda disponible en `http://localhost:8000`. Documentación interactiva en `http://localhost:8000/docs`.

## Tests

```bash
pytest
```

## Docker

```bash
docker build -t neuroinventory-ai-service .
docker run -p 8000:8000 --env-file ../.env neuroinventory-ai-service
```

O desde la raíz del repo:

```bash
docker compose up ai-service
```

## Estructura

```text
app/
├── api/           # endpoints
├── embeddings/    # generación de embeddings
├── rag/           # pipeline RAG
├── search/        # búsqueda semántica/vectorial
├── security/      # autenticación interna
├── config.py
└── dependencies.py
```
