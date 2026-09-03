package com.alexlizzt.inventory_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterMovementRequest(
    @Schema(
        description = "Identificador único del producto al que se aplicará el movimiento",
        example = "prod-123456"
    )
    @NotBlank(message = "El ID del producto es obligatorio")
    String productId,

    @Schema(
        description = "Tipo de movimiento de inventario. IN incrementa el stock, OUT lo disminuye y ADJUSTMENT permite corregir el stock.",
        example = "IN",
        allowableValues = {"IN", "OUT", "ADJUSTMENT"}
    )
    @NotBlank(message = "El tipo de movimiento es obligatorio")
    String type,

    @Schema(
        description = "Cantidad de unidades involucradas en el movimiento",
        example = "10",
        minimum = "1"
    )
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    Integer quantity,

    @Schema(
        description = "Motivo o justificación del movimiento de inventario",
        example = "Ingreso de mercancía por nueva compra"
    )
    String reason
) {

}
