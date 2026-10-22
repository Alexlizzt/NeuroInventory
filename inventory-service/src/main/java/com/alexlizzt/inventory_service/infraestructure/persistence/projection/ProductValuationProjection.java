package com.alexlizzt.inventory_service.infraestructure.persistence.projection;

import java.math.BigDecimal;

/**
 * Proyección de la valoración de inventario por producto.
 */
public interface ProductValuationProjection {

    String getProductId();

    String getSku();

    String getProductName();

    String getCategoryId();

    String getCategoryName();

    Long getQuantity();

    Long getMinStock();

    BigDecimal getUnitPrice();

    BigDecimal getTotalValue();

    Long getMovementCount();
}
