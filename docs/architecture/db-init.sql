-- =========================================================================
-- NeuroInventory Database Schema
-- Compatible con PostgreSQL y pgvector
-- =========================================================================

-- 1. Habilitar la extensión para búsqueda vectorial (requerida por el AI Service)
CREATE EXTENSION IF NOT EXISTS vector;

-- =========================================================================
-- 2. CAPA RELACIONAL (Gestionada por el Inventory Service)
-- =========================================================================

-- Tabla: Category (Clasificación de productos)
CREATE TABLE categories (
    id VARCHAR(36) PRIMARY KEY, -- UUID o identificador inmutable
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: Product (Información principal del inventario)
CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) 
        REFERENCES categories(id) 
        ON DELETE RESTRICT
);

-- Tabla: Stock (Estado actual de existencias)
CREATE TABLE stocks (
    product_id VARCHAR(36) PRIMARY KEY,
    quantity INT NOT NULL CHECK (quantity >= 0),
    min_stock INT NOT NULL DEFAULT 0 CHECK (min_stock >= 0),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_product FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
);

-- Tabla: InventoryMovement (Registro histórico de entradas, salidas y ajustes)
CREATE TABLE inventory_movements (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    type VARCHAR(30) NOT NULL, -- Ej: IN, OUT, ADJUSTMENT
    quantity INT NOT NULL CHECK (quantity > 0),
    reason TEXT,
    user_id VARCHAR(36), -- Referencia lógica a Keycloak (sin FK estricta entre servicios)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_movement_product FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE RESTRICT
);

-- =========================================================================
-- 3. CAPA VECTORIAL (Gestionada por el AI Service)
-- =========================================================================

-- Tabla: product_embeddings (Embeddings para búsqueda semántica de productos)
CREATE TABLE product_embeddings (
    product_id VARCHAR(36) PRIMARY KEY,
    embedding vector(1536), -- Dimensión estándar típica (ej. OpenAI text-embedding-3-small)
    metadata JSONB,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_embedding_product FOREIGN KEY (product_id) 
        REFERENCES products(id) 
        ON DELETE CASCADE
);

-- Tabla: document_embeddings (Fragmentos de documentación para RAG)
CREATE TABLE document_embeddings (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL,
    embedding vector(1536),
    source VARCHAR(255),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================================
-- 4. ÍNDICES DE RENDIMIENTO
-- =========================================================================

-- Índices relacionales comunes
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_movements_product ON inventory_movements(product_id);

-- Índice vectorial (HNSW o IVFFlat) para búsquedas semánticas rápidas en pgvector
-- Usamos HNSW por su excelente relación velocidad/precisión en la mayoría de casos
CREATE INDEX idx_product_embeddings_hnsw ON product_embeddings 
USING hnsw (embedding vector_cosine_ops);