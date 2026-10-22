package com.alexlizzt.inventory_service.domain.model;

/**
 * Granularidad temporal utilizada para agrupar los movimientos de inventario
 * en los reportes de series de tiempo.
 */
public enum ReportGranularity {
    DAY,
    WEEK,
    MONTH
}
