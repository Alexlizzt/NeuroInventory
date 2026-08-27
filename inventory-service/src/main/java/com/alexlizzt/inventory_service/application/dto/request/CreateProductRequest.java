package com.alexlizzt.inventory_service.application.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
    @NotBlank(message = "El ID de categoría es obligatorio")
    String categoryId,

    @NotBlank(message = "El SKU es obligatorio")
    @Size(max = 50, message = "El SKU no puede exceder los 50 caracteres")
    String sku,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres")
    String name,

    String description,

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = true, message = "El precio debe ser mayor o igual a 0")
    BigDecimal price,

    @NotNull(message = "El stock inicial es obligatorio")
    Integer initialStock,

    @NotNull(message = "El stock mínimo es obligatorio")
    Integer minStock
) {
    
}
