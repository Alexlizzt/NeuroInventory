package com.alexlizzt.inventory_service.application.command;

import java.math.BigDecimal;

public record UpdateProductCommand(
    String id,
    String name,
    String description,
    BigDecimal price,
    String categoryId,
    Boolean active
) {
    public UpdateProductCommand {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Product ID cannot be null or empty");
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }
}