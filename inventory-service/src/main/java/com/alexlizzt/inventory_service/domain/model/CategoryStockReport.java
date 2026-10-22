package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;

/**
 * Reporte de inventario agrupado por categoría.
 *
 * @param categoryId    identificador de la categoría
 * @param categoryName  nombre de la categoría
 * @param productCount  cantidad de productos asociados
 * @param totalUnits    unidades almacenadas en la categoría
 * @param totalValue    valor monetario acumulado de la categoría
 * @param lowStockCount productos de la categoría con stock crítico
 */
public record CategoryStockReport(
        String categoryId,
        String categoryName,
        long productCount,
        long totalUnits,
        BigDecimal totalValue,
        long lowStockCount) {
}
