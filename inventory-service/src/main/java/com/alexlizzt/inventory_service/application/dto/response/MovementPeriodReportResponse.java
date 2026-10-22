package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "MovementPeriodReportResponse",
    description = "Punto de la serie temporal de movimientos de inventario"
)
public record MovementPeriodReportResponse(
    @Schema(description = "Etiqueta del período según la granularidad aplicada", example = "2026-10-15")
    String period,

    @Schema(description = "Unidades de entrada en el período", example = "120")
    long inboundUnits,

    @Schema(description = "Unidades de salida en el período", example = "80")
    long outboundUnits,

    @Schema(description = "Unidades de ajuste en el período", example = "5")
    long adjustmentUnits,

    @Schema(description = "Flujo neto de unidades del período", example = "40")
    long netUnits,

    @Schema(description = "Movimientos de entrada del período", example = "12")
    long inboundMovements,

    @Schema(description = "Movimientos de salida del período", example = "9")
    long outboundMovements,

    @Schema(description = "Valor monetario de las entradas", example = "15400.00")
    BigDecimal inboundValue,

    @Schema(description = "Valor monetario de las salidas", example = "9800.00")
    BigDecimal outboundValue,

    @Schema(description = "Flujo neto monetario del período", example = "5600.00")
    BigDecimal netValue
) {
}
