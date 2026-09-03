package com.alexlizzt.inventory_service.application.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "InventoryMovementResponse",
    description = "Información de un movimiento realizado en el inventario"
)
public record InventoryMovementResponse(
    @Schema(
        description = "Identificador único del movimiento de inventario",
        example = "mov-123456"
    )
    String id,

    @Schema(
        description = "Identificador único del producto afectado por el movimiento",
        example = "prod-123456"
    )
    String productId,

    @Schema(
        description = "Tipo de movimiento realizado. IN incrementa el stock, OUT lo disminuye y ADJUSTMENT permite corregirlo",
        example = "IN",
        allowableValues = {"IN", "OUT", "ADJUSTMENT"}
    )
    String type,

    @Schema(
        description = "Cantidad de unidades involucradas en el movimiento",
        example = "10",
        minimum = "1"
    )
    Integer quantity,

    @Schema(
        description = "Motivo o justificación del movimiento de inventario",
        example = "Ingreso de mercancía por nueva compra"
    )
    String reason,

    @Schema(
        description = "Identificador del usuario que realizó el movimiento",
        example = "user-123456"
    )
    String userId,

    @Schema(
        description = "Fecha y hora en la que se registró el movimiento",
        example = "2026-09-03T16:30:00"
    )
    LocalDateTime createdAt
) {

}
