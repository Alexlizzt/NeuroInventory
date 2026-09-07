package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;

@FunctionalInterface 
public interface GetInventoryHistoryUseCase {
    List<InventoryMovementResponse> execute(String productId);
}
