package com.alexlizzt.inventory_service.infraestructure.persistence.projection;

import java.math.BigDecimal;

/**
 * Proyección del inventario agrupado por categoría.
 */
public interface CategoryStockProjection {

    String getCategoryId();

    String getCategoryName();

    Long getProductCount();

    Long getTotalUnits();

    BigDecimal getTotalValue();

    Long getLowStockCount();
}
