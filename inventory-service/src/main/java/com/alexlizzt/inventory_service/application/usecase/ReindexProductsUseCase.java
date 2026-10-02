package com.alexlizzt.inventory_service.application.usecase;

public interface ReindexProductsUseCase {

    /**
     * Regenera los embeddings de todos los productos existentes.
     *
     * @return cantidad de productos indexados
     */
    int execute();
}
