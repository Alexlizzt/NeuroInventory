package com.alexlizzt.inventory_service.domain.port;

import java.util.List;

import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.SemanticMatch;


public interface AiServicePort {
    List<SemanticMatch> searchSemantically(String query, int limit);

    /**
     * Genera (o actualiza) el embedding de un producto en el motor de búsqueda semántica.
     *
     * @return {@code true} si el producto quedó indexado correctamente.
     */
    boolean indexProduct(Product product);

    /**
     * Elimina el embedding asociado a un producto del motor de búsqueda semántica.
     *
     * @return {@code true} si la operación se completó correctamente.
     */
    boolean deleteProductIndex(String productId);
}
