# ============================================================
# Stage 1: Build
# ============================================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew

# Descarga dependencias y construye el JAR
COPY src ./src

RUN ./gradlew clean bootJar --no-daemon


# ============================================================
# Stage 2: Runtime
# ============================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
