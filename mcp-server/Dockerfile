# syntax=docker/dockerfile:1

FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1

WORKDIR /app

# Usuario sin privilegios para el proceso de la aplicación.
RUN groupadd --system app \
    && useradd --system --gid app --create-home --home-dir /home/app app

COPY requirements.txt .
RUN --mount=type=cache,target=/root/.cache/pip \
    pip install -r requirements.txt

COPY --chown=app:app app ./app
COPY --chown=app:app main.py .

USER app

# Puerto del servidor MCP (SSE)
EXPOSE 8001

# Los secretos (INTERNAL_API_KEY, SERVICE_TOKEN) se inyectan por entorno desde
# docker-compose/.env; nunca se hornean en la imagen.
ENV INVENTORY_SERVICE_URL=http://inventory-service:8080 \
    AI_SERVICE_URL=http://ai-service:8000 \
    MCP_HOST=0.0.0.0 \
    MCP_PORT=8001

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD python -c "import socket; socket.create_connection(('127.0.0.1', 8001), 3).close()"

CMD ["python", "main.py"]
