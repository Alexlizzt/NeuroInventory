# Modelo de Datos

El esquema está definido en `docs/architecture/db-init.sql` (capa relacional + vectorial) y las migraciones Flyway del `inventory-service`.

## Diagrama de entidades

```mermaid
erDiagram
    categories ||--o{ products : clasifica
    products ||--|| stocks : tiene
    products ||--o{ inventory_movements : registra
    products ||--o| product_embeddings : "embedding (sin FK)"

    categories {
        VARCHAR(36) id PK
        VARCHAR(100) name UK
        TEXT description
    }

    products {
        VARCHAR(36) id PK
        VARCHAR(36) category_id FK
        VARCHAR(50) sku UK
        VARCHAR(150) name
        decimal price
        BOOLEAN active
    }

    stocks {
        VARCHAR(36) product_id PK_FK
        INT quantity
        INT min_stock
    }

    inventory_movements {
        VARCHAR(36) id PK
        VARCHAR(36) product_id FK
        VARCHAR(30) type "IN, OUT, ADJUSTMENT"
        INT quantity
        VARCHAR(36) user_id "ref. lógica a Keycloak"
    }

    product_embeddings {
        VARCHAR(36) product_id PK
        vector(768) embedding
        JSONB metadata
    }

    document_embeddings {
        VARCHAR(36) id PK
        TEXT content
        vector(768) embedding
        VARCHAR(255) source
        JSONB metadata
    }
```

## Capa relacional (Inventory Service)

| Tabla | Descripción |
| --- | --- |
| `categories` | Clasificación de productos. |
| `products` | Información principal del inventario (SKU único, precio, estado activo). |
| `stocks` | Existencias actuales por producto (relación 1:1). |
| `inventory_movements` | Historial de entradas, salidas y ajustes. `user_id` referencia lógicamente a Keycloak (sin FK estricta). |

## Capa vectorial (AI Service)

| Tabla | Descripción |
| --- | --- |
| `product_embeddings` | Embeddings (768 dims, `nomic-embed-text`) para búsqueda semántica de productos. Sin FK hacia `products` para mantener el desacoplamiento; la limpieza se hace explícitamente. |
| `document_embeddings` | Fragmentos de documentación con embeddings para RAG. |

## Índices

- Relacionales: `products(category_id)`, `products(sku)`, `inventory_movements(product_id)`.
- Vectorial: índice **HNSW** (`vector_cosine_ops`) sobre `product_embeddings.embedding` para búsquedas por similitud rápidas.
