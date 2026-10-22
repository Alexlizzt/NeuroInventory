package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "CategoryStockReportResponse",
    description = "Inventario agrupado por categoría"
)
public record CategoryStockReportResponse(
    @Schema(description = "Identificador de la categoría", example = "cat-123456")
    String categoryId,

    @Schema(description = "Nombre de la categoría", example = "Laptops")
    String categoryName,

    @Schema(description = "Cantidad de productos de la categoría", example = "18")
    long productCount,

    @Schema(description = "Unidades almacenadas en la categoría", example = "342")
    long totalUnits,

    @Schema(description = "Valor monetario acumulado de la categoría", example = "48200.50")
    BigDecimal totalValue,

    @Schema(description = "Porcentaje que representa la categoría sobre el valor total", example = "37.53")
    double percentage,

    @Schema(description = "Productos de la categoría con stock crítico", example = "4")
    long lowStockCount
) {
}
