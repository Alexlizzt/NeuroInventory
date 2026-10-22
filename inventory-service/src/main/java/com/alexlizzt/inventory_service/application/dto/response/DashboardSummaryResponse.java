package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "DashboardSummaryResponse",
    description = "Indicadores agregados del inventario para el dashboard"
)
public record DashboardSummaryResponse(
    @Schema(description = "Cantidad total de productos registrados", example = "128")
    long totalProducts,

    @Schema(description = "Cantidad de productos activos", example = "120")
    long activeProducts,

    @Schema(description = "Cantidad total de categorías", example = "8")
    long totalCategories,

    @Schema(description = "Unidades totales almacenadas", example = "5432")
    long totalUnits,

    @Schema(description = "Productos con stock en o por debajo del mínimo", example = "12")
    long lowStockProducts,

    @Schema(description = "Productos agotados", example = "3")
    long outOfStockProducts,

    @Schema(description = "Valor monetario total del inventario", example = "128450.99")
    BigDecimal inventoryValue,

    @Schema(description = "Movimientos de entrada registrados en el período", example = "48")
    long inboundMovements,

    @Schema(description = "Movimientos de salida registrados en el período", example = "31")
    long outboundMovements,

    @Schema(description = "Unidades de entrada registradas en el período", example = "820")
    long inboundUnits,

    @Schema(description = "Unidades de salida registradas en el período", example = "455")
    long outboundUnits,

    @Schema(description = "Flujo neto de unidades del período", example = "365")
    long netUnits,

    @Schema(description = "Inicio del período analizado", example = "2026-09-21T00:00:00")
    LocalDateTime periodStart,

    @Schema(description = "Fin del período analizado", example = "2026-10-21T23:59:59")
    LocalDateTime periodEnd,

    @Schema(description = "Momento de generación del reporte", example = "2026-10-21T18:00:00")
    LocalDateTime generatedAt
) {
}
