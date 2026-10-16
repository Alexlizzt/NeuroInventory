FROM python:3.12-slim

WORKDIR /app

# Instalar dependencias
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiar código de la aplicación
COPY app ./app
COPY main.py .

# Puerto del servidor MCP (SSE)
EXPOSE 8001

# Variables de entorno por defecto (sobrescritas por docker-compose / .env)
ENV INVENTORY_SERVICE_URL=http://inventory-service:8080
ENV AI_SERVICE_URL=http://ai-service:8000
ENV INTERNAL_API_KEY=neuroinventory-secret-key-2026
ENV MCP_HOST=0.0.0.0
ENV MCP_PORT=8001

CMD ["python", "main.py"]
