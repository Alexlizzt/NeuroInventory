package com.alexlizzt.inventory_service.infraestructure.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Proyección del detalle de movimientos de inventario.
 */
public interface MovementDetailProjection {

    String getMovementId();

    String getProductId();

    String getSku();

    String getProductName();

    String getCategoryId();

    String getCategoryName();

    String getType();

    Long getQuantity();

    BigDecimal getUnitPrice();

    BigDecimal getTotalValue();

    String getReason();

    String getUserId();

    LocalDateTime getCreatedAt();
}
