# syntax=docker/dockerfile:1

# ============================================================
# Stage 1: Build
# ============================================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN chmod +x gradlew

# Los ficheros de build se copian antes que el código. La caché de Gradle se
# conserva entre builds con un cache mount de BuildKit, así que un cambio de
# fuentes no vuelve a descargar las dependencias.
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew clean bootJar --no-daemon


# ============================================================
# Stage 2: Runtime (JRE Alpine, usuario sin privilegios)
# ============================================================
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

COPY --from=builder --chown=app:app /app/build/libs/*.jar app.jar

USER app

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8080/api/v1/inventory-service/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
