package com.alexlizzt.inventory_service.application.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterMovementRequest(
    @NotBlank(message = "El ID del producto es obligatorio")
    String productId,

    @NotBlank(message = "El tipo de movimiento es obligatorio (IN, OUT, ADJUSTMENT)")
    String type,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a 0")
    Integer quantity,

    String reason
) {

}
