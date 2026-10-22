package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;

/**
 * Resumen agregado del inventario usado como fuente del dashboard.
 *
 * @param totalProducts      cantidad total de productos registrados
 * @param activeProducts     cantidad de productos activos
 * @param totalCategories    cantidad de categorías registradas
 * @param totalUnits         unidades totales almacenadas
 * @param lowStockProducts   productos cuyo stock está en o por debajo del mínimo
 * @param outOfStockProducts productos agotados
 * @param inventoryValue     valor monetario del inventario (unidades x precio)
 * @param inboundMovements   movimientos de entrada registrados en el período
 * @param outboundMovements  movimientos de salida registrados en el período
 * @param inboundUnits       unidades de entrada registradas en el período
 * @param outboundUnits      unidades de salida registradas en el período
 */
public record InventorySummary(
        long totalProducts,
        long activeProducts,
        long totalCategories,
        long totalUnits,
        long lowStockProducts,
        long outOfStockProducts,
        BigDecimal inventoryValue,
        long inboundMovements,
        long outboundMovements,
        long inboundUnits,
        long outboundUnits) {

    /**
     * Flujo neto de unidades del período (entradas menos salidas).
     */
    public long netUnits() {
        return inboundUnits - outboundUnits;
    }
}
