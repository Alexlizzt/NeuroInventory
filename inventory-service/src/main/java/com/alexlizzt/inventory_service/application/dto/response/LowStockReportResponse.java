package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "LowStockReportResponse",
    description = "Reporte avanzado de productos con inventario crítico o por debajo del mínimo"
)
public record LowStockReportResponse(
    @Schema(description = "Identificador del producto", example = "prod-123456")
    String productId,

    @Schema(description = "Código SKU del producto", example = "LP-1001")
    String sku,

    @Schema(description = "Nombre del producto", example = "Laptop Pro 14")
    String productName,

    @Schema(description = "Nombre de la categoría", example = "Laptops")
    String categoryName,

    @Schema(description = "Unidades en stock", example = "3")
    long quantity,

    @Schema(description = "Stock mínimo configurado", example = "10")
    long minStock,

    @Schema(description = "Unidades faltantes para cubrir el stock mínimo", example = "7")
    long shortage,

    @Schema(description = "Porcentaje de cobertura respecto al stock mínimo", example = "30.00")
    double coveragePercentage,

    @Schema(description = "Precio unitario del producto", example = "1299.90")
    BigDecimal unitPrice,

    @Schema(description = "Valor del stock actual", example = "3899.70")
    BigDecimal totalValue,

    @Schema(description = "Costo estimado para reponer el stock mínimo", example = "9099.30")
    BigDecimal suggestedPurchaseValue,

    @Schema(description = "Severidad del nivel de inventario", example = "WARNING")
    String severity
) {
}
