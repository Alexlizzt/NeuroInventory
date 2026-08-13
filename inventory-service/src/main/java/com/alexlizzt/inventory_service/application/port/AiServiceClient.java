package com.alexlizzt.inventory_service.application.port;

import java.util.List;

import com.alexlizzt.inventory_service.domain.model.Product;

public interface AiServiceClient {
    List<Product> searchSemantically(String query, int limit);
}
