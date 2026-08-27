package com.alexlizzt.inventory_service.application.dto.response;

import java.time.LocalDateTime;

public record InventoryMovementResponse(
    String id,
    String productId,
    String type,
    Integer quantity,
    String reason,
    String userId,
    LocalDateTime createdAt
) {

}
