# System Context

## ¿Qué es NeuroInventory?

NeuroInventory es una plataforma de gestión de inventario que combina un backend empresarial tradicional con capacidades de inteligencia artificial (búsqueda semántica, RAG y modelos LLM locales).

## Sistemas con los que interactúa

```mermaid
flowchart LR

    USER[Usuario / Operador]

    AGENT[Agente IA compatible MCP<br/>(roadmap)]

    NI[NeuroInventory<br/>Frontend + Inventory Service<br/>+ AI Service]

    KC[Keycloak<br/>Proveedor de identidad]

    DB[(PostgreSQL + pgvector)]

    LLM[Ollama<br/>LLM local]

    USER -->|HTTPS / navegador| NI
    NI -->|OAuth2 / OIDC| KC
    NI -->|SQL / JDBC| DB
    NI -->|Generación de texto y embeddings| LLM
    AGENT -.->|MCP tools| NI
```

## Actores y sistemas externos

| Sistema | Relación |
| --- | --- |
| **Usuario** | Interactúa con la plataforma a través del frontend web (Angular). |
| **Keycloak** | Centraliza autenticación y autorización. Emite y valida tokens JWT. |
| **PostgreSQL + pgvector** | Almacena datos transaccionales del inventario y embeddings vectoriales. |
| **Ollama** | Ejecuta los modelos LLM locales para RAG y asistente de IA. |
| **Agente IA (MCP)** | (Roadmap) Consumirá las capacidades del dominio como herramientas MCP. |

## Notas

- Todos los componentes se ejecutan localmente mediante Docker Compose; no hay dependencias de servicios cloud externos en la fase actual.
- Los modelos LLM son locales, por lo que ningún dato sale del entorno del usuario.
