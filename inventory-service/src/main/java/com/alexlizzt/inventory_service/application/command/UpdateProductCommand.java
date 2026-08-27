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

}
