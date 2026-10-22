package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "MovementDetailReportResponse",
    description = "Detalle de un movimiento de inventario enriquecido con producto y categoría"
)
public record MovementDetailReportResponse(
    @Schema(description = "Identificador del movimiento", example = "mov-123456")
    String movementId,

    @Schema(description = "Identificador del producto", example = "prod-123456")
    String productId,

    @Schema(description = "Código SKU del producto", example = "LP-1001")
    String sku,

    @Schema(description = "Nombre del producto", example = "Laptop Pro 14")
    String productName,

    @Schema(description = "Identificador de la categoría", example = "cat-123456")
    String categoryId,

    @Schema(description = "Nombre de la categoría", example = "Laptops")
    String categoryName,

    @Schema(description = "Tipo de movimiento", example = "IN")
    String type,

    @Schema(description = "Cantidad de unidades del movimiento", example = "20")
    long quantity,

    @Schema(description = "Precio unitario vigente del producto", example = "1299.90")
    BigDecimal unitPrice,

    @Schema(description = "Valor monetario del movimiento", example = "25998.00")
    BigDecimal totalValue,

    @Schema(description = "Motivo registrado en el movimiento", example = "Compra a proveedor")
    String reason,

    @Schema(description = "Usuario responsable del movimiento", example = "user-123456")
    String userId,

    @Schema(description = "Fecha y hora de registro del movimiento", example = "2026-10-15T10:30:00")
    LocalDateTime createdAt
) {
}
