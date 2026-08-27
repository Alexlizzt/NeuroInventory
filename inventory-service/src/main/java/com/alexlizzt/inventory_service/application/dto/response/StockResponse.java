package com.alexlizzt.inventory_service.application.dto.response;

import java.time.LocalDateTime;

public record StockResponse(
    String productId,
    Integer quantity,
    Integer minStock,
    Boolean isLowStock,
    LocalDateTime updatedAt
) {

}
