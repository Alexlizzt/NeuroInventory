package com.alexlizzt.inventory_service.domain.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.alexlizzt.inventory_service.domain.model.CategoryStockReport;
import com.alexlizzt.inventory_service.domain.model.InventorySummary;
import com.alexlizzt.inventory_service.domain.model.MovementDetailReport;
import com.alexlizzt.inventory_service.domain.model.MovementPeriodReport;
import com.alexlizzt.inventory_service.domain.model.ProductValuationReport;
import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;

/**
 * Puerto de acceso a los datos agregados necesarios para los reportes
 * de inventario. La implementación reside en la capa de infraestructura.
 */
public interface InventoryReportRepository {

    /**
     * Resumen agregado del inventario con los indicadores del período indicado.
     */
    InventorySummary summarize(LocalDateTime from, LocalDateTime to);

    /**
     * Inventario agrupado por categoría.
     */
    List<CategoryStockReport> findStockByCategory();

    /**
     * Serie temporal de movimientos agrupados por la granularidad solicitada.
     */
    List<MovementPeriodReport> findMovementsByPeriod(
            ReportGranularity granularity,
            LocalDateTime from,
            LocalDateTime to);

    /**
     * Productos destacados ordenados por la métrica indicada.
     */
    List<ProductValuationReport> findTopProducts(
            ReportMetric metric,
            int limit,
            LocalDateTime from,
            LocalDateTime to);

    /**
     * Productos con stock en o por debajo del mínimo configurado.
     */
    List<ProductValuationReport> findLowStock();

    /**
     * Detalle de movimientos filtrado por período, tipo y categoría.
     */
    List<MovementDetailReport> findMovements(
            LocalDateTime from,
            LocalDateTime to,
            String type,
            String categoryId,
            int limit);
}
