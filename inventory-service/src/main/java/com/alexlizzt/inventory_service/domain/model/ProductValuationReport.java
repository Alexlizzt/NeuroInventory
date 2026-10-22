package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;

/**
 * Reporte de valoración de un producto dentro del inventario.
 *
 * @param productId     identificador del producto
 * @param sku           código SKU del producto
 * @param productName   nombre del producto
 * @param categoryId    identificador de la categoría
 * @param categoryName  nombre de la categoría
 * @param quantity      unidades en stock
 * @param minStock      stock mínimo configurado
 * @param unitPrice     precio unitario del producto
 * @param totalValue    valor monetario del stock (unidades x precio)
 * @param movementCount cantidad de movimientos registrados para el producto
 */
public record ProductValuationReport(
        String productId,
        String sku,
        String productName,
        String categoryId,
        String categoryName,
        long quantity,
        long minStock,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        long movementCount) {

    /**
     * Faltante de unidades respecto al stock mínimo configurado.
     */
    public long shortage() {
        return Math.max(0, minStock - quantity);
    }

    /**
     * Severidad del producto según su cobertura de inventario.
     */
    public StockSeverity severity() {
        if (quantity == 0) {
            return StockSeverity.CRITICAL;
        }
        if (quantity <= minStock) {
            return StockSeverity.WARNING;
        }
        return StockSeverity.HEALTHY;
    }
}
