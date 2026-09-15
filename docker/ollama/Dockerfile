FROM ollama/ollama:latest

# Inicia el servidor temporalmente durante la construcción para hacer el pull
RUN ./bin/ollama serve & \
    sleep 5 && \
    ollama pull nomic-embed-text && \
    ollama pull llama3.2