package com.alexlizzt.inventory_service.infraestructure.web.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(
    name = "CreateProductRequest",
    description = "Datos necesarios para crear un nuevo producto en el inventario"
)
public record CreateProductRequest(
    @Schema(
        description = "Identificador único de la categoría a la que pertenece el producto",
        example = "cat-123456"
    )
    @NotBlank(message = "El ID de la categoría es obligatorio")
    String categoryId,

    @Schema(
        description = "Código único utilizado para identificar el producto en el inventario",
        example = "SKU-00123"
    )
    @NotBlank(message = "El SKU es obligatorio")
    String sku,

    @Schema(
        description = "Nombre del producto",
        example = "Teclado mecánico inalámbrico"
    )
    @NotBlank(message = "El nombre es obligatorio")
    String name,

    @Schema(
        description = "Descripción opcional del producto",
        example = "Teclado mecánico con iluminación RGB y conexión inalámbrica"
    )
    String description,

    @Schema(
        description = "Precio de venta del producto",
        example = "149.99",
        minimum = "0.01"
    )
    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor que cero")
    BigDecimal price,

    @Schema(
        description = "Cantidad inicial de unidades disponibles en el inventario",
        example = "50",
        minimum = "0"
    )
    @NotNull(message = "El stock inicial es obligatorio")
    @PositiveOrZero(message = "El stock inicial debe ser mayor o igual a cero")
    Integer initialStock,

    @Schema(
        description = "Cantidad mínima de unidades que debe mantenerse en inventario",
        example = "10",
        minimum = "0"
    )
    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo debe ser mayor o igual a cero")
    Integer minStock
) {

}
