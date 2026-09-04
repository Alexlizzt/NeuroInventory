package com.alexlizzt.inventory_service.application.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

@Schema(
    name = "UpdateProductRequest",
    description = "Datos necesarios para actualizar un producto existente"
)
public record UpdateProductRequest(

    @Schema(
        description = "Nombre del producto",
        example = "Laptop Pro 15",
        minLength = 2,
        maxLength = 150
    )
    @Size(
        min = 2,
        max = 150,
        message = "El nombre del producto debe tener entre 2 y 150 caracteres"
    )
    String name,

    @Schema(
        description = "Descripción detallada del producto",
        example = "Laptop de alta gama con 32GB RAM y 1TB SSD",
        maxLength = 500
    )
    @Size(
        max = 500,
        message = "La descripción no puede superar los 500 caracteres"
    )
    String description,

    @Schema(
        description = "Precio unitario del producto",
        example = "1299.99",
        minimum = "0.0"
    )
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "El precio no puede ser negativo"
    )
    BigDecimal price,

    @Schema(
        description = "Identificador único de la categoría a la que pertenece",
        example = "a1b2c3d4-0000-0000-0000-1234567890ab"
    )
    String categoryId,

    @Schema(
        description = "Estado de disponibilidad operativa del producto en el catálogo",
        example = "true"
    )
    Boolean active
) {}