package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ProductResponse",
    description = "Información de un producto del inventario"
)
public record ProductResponse(
    @Schema(
        description = "Identificador único del producto",
        example = "prod-123456"
    )
    String id,

    @Schema(
        description = "Código único utilizado para identificar el producto en el inventario",
        example = "SKU-00123"
    )
    String sku,

    @Schema(
        description = "Nombre del producto",
        example = "Teclado mecánico inalámbrico"
    )
    String name,

    @Schema(
        description = "Descripción del producto",
        example = "Teclado mecánico con iluminación RGB y conexión inalámbrica"
    )
    String description,

    @Schema(
        description = "Precio de venta del producto",
        example = "149.99"
    )
    BigDecimal price,

    @Schema(
        description = "Indica si el producto se encuentra activo",
        example = "true"
    )
    Boolean active,

    @Schema(
        description = "Identificador de la categoría a la que pertenece el producto",
        example = "cat-123456"
    )
    String categoryId,

    @Schema(
        description = "Fecha y hora en la que se creó el producto",
        example = "2026-09-03T15:30:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Fecha y hora de la última actualización del producto",
        example = "2026-09-03T16:45:00"
    )
    LocalDateTime updatedAt
) {

}
