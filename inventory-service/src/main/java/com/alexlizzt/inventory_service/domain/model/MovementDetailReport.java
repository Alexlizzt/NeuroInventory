package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detalle de un movimiento de inventario enriquecido con los datos del
 * producto y su categoría, utilizado por el reporte detallado de movimientos.
 *
 * @param movementId   identificador del movimiento
 * @param productId    identificador del producto
 * @param sku          código SKU del producto
 * @param productName  nombre del producto
 * @param categoryId   identificador de la categoría
 * @param categoryName nombre de la categoría
 * @param type         tipo de movimiento (IN, OUT, ADJUSTMENT)
 * @param quantity     cantidad de unidades del movimiento
 * @param unitPrice    precio unitario vigente del producto
 * @param totalValue   valor monetario del movimiento
 * @param reason       motivo registrado
 * @param userId       usuario responsable del movimiento
 * @param createdAt    fecha y hora de registro del movimiento
 */
public record MovementDetailReport(
        String movementId,
        String productId,
        String sku,
        String productName,
        String categoryId,
        String categoryName,
        String type,
        long quantity,
        BigDecimal unitPrice,
        BigDecimal totalValue,
        String reason,
        String userId,
        LocalDateTime createdAt) {
}
