# 📦 NeuroInventory

Backend de Inventario Inteligente con Búsqueda Semántica
Arquitectura basada en microservicios, OAuth2 y AI embeddings.

## 🚀 Overview

NeuroInventory es una plataforma moderna diseñada para gestionar inventario empresarial con capacidades avanzadas de búsqueda semántica mediante modelos de IA.

El sistema permite:

  - Gestión de productos, categorías y stock
  - Control de movimientos de inventario
  - Seguridad basada en roles (RBAC)
  - Búsqueda semántica de productos usando embeddings
  - Arquitectura desacoplada y escalable

## 🏗 Arquitectura

El sistema está compuesto por:

inventory-service → API REST en Spring Boot

ai-service → Servicio de IA en FastAPI

PostgreSQL → Base de datos relacional

Keycloak → Gestión de identidad y acceso

Docker + Docker Compose para orquestación

## 🔷 Modelo Arquitectónico

Se utiliza el modelo C4 propuesto por Simon Brown.

La arquitectura sigue principios de:

 - Clean Architecture
 - Domain-Driven Design (DDD)
 - Microservicios ligeros
 - OAuth2 / OpenID Connect
 - Containerización

Diagramas disponibles en:
~~~bash
/docs/architecture
~~~
## 🔐 Seguridad

La autenticación y autorización están gestionadas mediante:

Keycloak
OAuth2 Resource Server en Spring
JWT validation

Control de acceso basado en roles:

Roles definidos:

ROLE_ADMIN
ROLE_OPERATOR

Flujo:

Usuario se autentica en Keycloak
Keycloak emite JWT
inventory-service valida token
Acceso autorizado según roles

## 🧠 Búsqueda Semántica

El sistema incorpora un microservicio de IA desarrollado con:

FastAPI

Modelos de embeddings (Sentence Transformers)

Proceso:

Se genera embedding del producto
Se almacena en PostgreSQL (pgvector)
Las búsquedas convierten el texto en vector
Se calcula similitud semántica
Esto permite búsquedas inteligentes como:

"Laptop ligera para oficina"
→ encuentra productos relacionados aunque no coincidan exactamente las palabras.

## 📁 Estructura del Proyecto
NeuroInventory/

## ⚙️ Tecnologías Utilizadas
Backend

- Java 21
- Spring Boot
- Spring Security
- JPA / Hibernate

AI Service

- Python 3.11
- FastAPI
- Sentence Transformers

Base de Datos

- PostgreSQL
- pgvector

Seguridad

- Keycloak
- OAuth2 / OIDC

Infraestructura

- Docker
- Docker Compose

GitHub Actions (CI/CD)

## 🐳 Ejecución Local
1️⃣ Clonar repositorio
~~~bash
git clone https://github.com/alexlizzt/NeuroInventory.git
cd NeuroInventory
~~~
2️⃣ Levantar servicios
~~~bash
docker-compose up --build
~~~

Servicios disponibles:

Inventory API → http://localhost:8080

AI Service → http://localhost:8000

Keycloak → http://localhost:8081

PostgreSQL → localhost:5432

## 📖 Documentación API

La documentación OpenAPI está disponible en:

/swagger-ui.html (Spring)

/docs (FastAPI)

## 🧪 Testing
Inventory Service

Unit tests (JUnit 5)

Integration tests

Testcontainers

AI Service

Pytest

Tests de endpoints

Mock embeddings

Ejecutar:
~~~bash
mvn test
pytest
~~~

## 🔄 CI/CD

El pipeline de Jenkins realiza:

Build automático

Ejecución de tests

Análisis estático

Build de imágenes Docker


## 🏛 Decisiones Arquitectónicas

Las decisiones clave están documentadas en:

/docs/adr


## 📊 Observabilidad

Incluye:

Spring Boot Actuator

Health checks

Logs estructurados

Métricas básicas

## 📌 Roadmap

- [ ] Caching con Redis
- [ ] Auditoría avanzada
- [ ] Dashboard frontend
- [ ] Event-driven architecture

## 📄 Licencia

MIT License