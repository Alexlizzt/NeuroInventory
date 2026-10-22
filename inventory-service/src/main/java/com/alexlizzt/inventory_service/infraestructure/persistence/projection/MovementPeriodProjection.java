package com.alexlizzt.inventory_service.infraestructure.persistence.projection;

import java.math.BigDecimal;

/**
 * Proyección de la serie temporal de movimientos de inventario.
 */
public interface MovementPeriodProjection {

    String getPeriod();

    Long getInboundUnits();

    Long getOutboundUnits();

    Long getAdjustmentUnits();

    Long getInboundMovements();

    Long getOutboundMovements();

    BigDecimal getInboundValue();

    BigDecimal getOutboundValue();
}
