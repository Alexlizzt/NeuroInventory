package com.alexlizzt.inventory_service.domain.model;

/**
 * Métrica de ordenamiento utilizada para el reporte de productos destacados.
 */
public enum ReportMetric {
    /** Ordena por valor monetario acumulado en inventario. */
    VALUE,
    /** Ordena por cantidad de movimientos registrados en el período. */
    MOVEMENT
}
