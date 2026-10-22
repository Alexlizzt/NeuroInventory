package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "ProductValuationReportResponse",
    description = "Valoración de inventario por producto"
)
public record ProductValuationReportResponse(
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

    @Schema(description = "Unidades en stock", example = "25")
    long quantity,

    @Schema(description = "Stock mínimo configurado", example = "10")
    long minStock,

    @Schema(description = "Precio unitario del producto", example = "1299.90")
    BigDecimal unitPrice,

    @Schema(description = "Valor monetario del stock del producto", example = "32497.50")
    BigDecimal totalValue,

    @Schema(description = "Cantidad de movimientos registrados para el producto", example = "14")
    long movementCount
) {
}
