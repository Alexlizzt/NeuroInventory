package com.alexlizzt.inventory_service.application.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "StockResponse",
    description = "Información del stock actual de un producto"
)
public record StockResponse(
    @Schema(
        description = "Identificador único del producto",
        example = "prod-123456"
    )
    String productId,

    @Schema(
        description = "Cantidad actual de unidades disponibles en el inventario",
        example = "25",
        minimum = "0"
    )
    Integer quantity,

    @Schema(
        description = "Cantidad mínima de unidades que debe mantenerse en inventario",
        example = "10",
        minimum = "0"
    )
    Integer minStock,

    @Schema(
        description = "Indica si la cantidad actual de stock es menor o igual al stock mínimo configurado",
        example = "false"
    )
    Boolean isLowStock,

    @Schema(
        description = "Fecha y hora de la última actualización del stock",
        example = "2026-09-03T16:30:00"
    )
    LocalDateTime updatedAt
) {

}
