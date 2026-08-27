package com.alexlizzt.inventory_service.application.command;

import java.math.BigDecimal;

public record CreateProductCommand(
    String categoryId,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Integer initialStock,
    Integer minStock
) {

}
