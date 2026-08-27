package com.alexlizzt.inventory_service.infraestructure.web.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductRequest(
    @NotBlank(message = "Category ID is required")
    String categoryId,

    @NotBlank(message = "SKU is required")
    String sku,

    @NotBlank(message = "Name is required")
    String name,

    String description,

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    BigDecimal price,

    @NotNull(message = "Initial stock is required")
    @Positive(message = "Initial stock must be zero or positive")
    Integer initialStock,

    @NotNull(message = "Minimum stock is required")
    @Positive(message = "Minimum stock must be zero or positive")
    Integer minStock
) {

}
