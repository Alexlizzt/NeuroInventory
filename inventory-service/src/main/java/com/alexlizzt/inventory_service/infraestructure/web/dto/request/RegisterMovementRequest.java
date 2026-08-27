package com.alexlizzt.inventory_service.infraestructure.web.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record RegisterMovementRequest(
    @NotBlank(message = "Product ID is required")
    String productId,

    @NotBlank(message = "Movement type is required")
    @Pattern(regexp = "^(?i)(IN|OUT|ADJUSTMENT)$", message = "Type must be IN, OUT, or ADJUSTMENT")
    String type,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    Integer quantity,

    @NotBlank(message = "Reason is required")
    String reason,

    @NotBlank(message = "User ID is required")
    String userId
) {

}
