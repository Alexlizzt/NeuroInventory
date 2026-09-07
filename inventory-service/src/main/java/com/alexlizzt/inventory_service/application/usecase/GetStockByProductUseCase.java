package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.StockResponse;

@FunctionalInterface 
public interface GetStockByProductUseCase {

    StockResponse execute(String productId);
}
