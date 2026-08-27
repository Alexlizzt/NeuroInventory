package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Boolean active,
    String categoryId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
