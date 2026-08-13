package com.alexlizzt.inventory_service.application.usecase.command;

import java.math.BigDecimal;

public record UpdateProductCommand(
    String id,
    String categoryId,
    String name,
    String description,
    BigDecimal price,
    Boolean active
) {

}
