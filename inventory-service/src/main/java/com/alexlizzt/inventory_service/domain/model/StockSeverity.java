package com.alexlizzt.inventory_service.domain.model;

/**
 * Severidad del nivel de inventario de un producto.
 */
public enum StockSeverity {
    /** Producto agotado: no hay unidades disponibles. */
    CRITICAL,
    /** Producto en o por debajo del stock mínimo configurado. */
    WARNING,
    /** Producto con cobertura de inventario saludable. */
    HEALTHY
}
