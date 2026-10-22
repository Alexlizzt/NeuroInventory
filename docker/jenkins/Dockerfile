# syntax=docker/dockerfile:1

# =============================================================================
# Jenkins de CI para NeuroInventory
#
# Incluye las toolchains del monorepo para no depender de lo instalado en el
# host:
#   - JDK 21  -> inventory-service (Gradle 9 + Testcontainers)
#   - Node 24 -> frontend (Angular)
#   - Python 3 -> ai-service (pytest) y mcp-server
#   - Docker CLI -> Testcontainers y construcción de imágenes
#
# El daemon de Docker NO se ejecuta aquí: se reutiliza el del host a través de
# /var/run/docker.sock (ver docker-compose.jenkins.yml).
# =============================================================================

FROM node:24-bookworm-slim AS node
FROM docker:27-cli AS dockercli

FROM jenkins/jenkins:lts-jdk21

USER root

# --- Docker CLI -------------------------------------------------------------
# Solo el cliente: habla con el daemon del host por el socket montado.
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker

# --- Node 24 ----------------------------------------------------------------
# Copiado de la imagen oficial (misma base Debian) para no depender del paquete
# nodejs de Debian, que en bookworm es demasiado antiguo para Angular.
COPY --from=node /usr/local/bin/node /usr/local/bin/node
COPY --from=node /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -sf /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm \
    && ln -sf /usr/local/lib/node_modules/npm/bin/npx-cli.js /usr/local/bin/npx

# --- Python y utilidades ----------------------------------------------------
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        python3 \
        python3-pip \
        python3-venv \
        unzip \
    && rm -rf /var/lib/apt/lists/*

# --- Plugins ----------------------------------------------------------------
# Se instalan en la imagen para que el primer arranque no dependa de
# updates.jenkins.io.
COPY docker/jenkins/plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# --- Configuración como código ---------------------------------------------
# Se guarda fuera de JENKINS_HOME para que el volumen no oculte las
# actualizaciones de la imagen.
COPY docker/jenkins/jenkins.yaml /usr/share/jenkins/ref/casc/jenkins.yaml
ENV CASC_JENKINS_CONFIG=/usr/share/jenkins/ref/casc

USER jenkins
