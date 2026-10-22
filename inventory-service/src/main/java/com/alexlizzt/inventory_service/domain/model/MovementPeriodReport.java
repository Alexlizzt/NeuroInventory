package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;

/**
 * Punto de una serie temporal de movimientos de inventario.
 *
 * @param period          etiqueta del período (depende de la granularidad)
 * @param inboundUnits    unidades de entrada acumuladas en el período
 * @param outboundUnits   unidades de salida acumuladas en el período
 * @param adjustmentUnits unidades de ajuste acumuladas en el período
 * @param inboundMovements  cantidad de movimientos de entrada
 * @param outboundMovements cantidad de movimientos de salida
 * @param inboundValue    valor monetario de las entradas
 * @param outboundValue   valor monetario de las salidas
 */
public record MovementPeriodReport(
        String period,
        long inboundUnits,
        long outboundUnits,
        long adjustmentUnits,
        long inboundMovements,
        long outboundMovements,
        BigDecimal inboundValue,
        BigDecimal outboundValue) {

    /**
     * Flujo neto de unidades del período.
     */
    public long netUnits() {
        return inboundUnits - outboundUnits;
    }

    /**
     * Flujo neto monetario del período.
     */
    public BigDecimal netValue() {
        return inboundValue.subtract(outboundValue);
    }
}
