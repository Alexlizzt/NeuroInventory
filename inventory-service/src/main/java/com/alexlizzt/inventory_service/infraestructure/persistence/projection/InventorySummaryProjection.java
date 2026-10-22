package com.alexlizzt.inventory_service.infraestructure.persistence.projection;

import java.math.BigDecimal;

/**
 * Proyección de la consulta agregada del resumen del inventario.
 */
public interface InventorySummaryProjection {

    Long getTotalProducts();

    Long getActiveProducts();

    Long getTotalCategories();

    Long getTotalUnits();

    Long getLowStockProducts();

    Long getOutOfStockProducts();

    BigDecimal getInventoryValue();

    Long getInboundMovements();

    Long getOutboundMovements();

    Long getInboundUnits();

    Long getOutboundUnits();
}
