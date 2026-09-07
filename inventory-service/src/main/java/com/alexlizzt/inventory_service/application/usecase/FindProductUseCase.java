package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;

@FunctionalInterface
public interface FindProductUseCase {

    ProductResponse execute(String id);
}
