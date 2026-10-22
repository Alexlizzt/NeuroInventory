package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.application.query.ReportQuery;

/**
 * Caso de uso que genera los reportes avanzados de inventario.
 */
public interface GenerateInventoryReportUseCase {

    /**
     * Indicadores agregados para el dashboard.
     */
    DashboardSummaryResponse getDashboardSummary(ReportQuery query);

    /**
     * Inventario agrupado por categoría con participación porcentual.
     */
    List<CategoryStockReportResponse> getStockByCategory();

    /**
     * Serie temporal de movimientos según la granularidad solicitada.
     */
    List<MovementPeriodReportResponse> getMovementsByPeriod(ReportQuery query);

    /**
     * Productos destacados por valor o por cantidad de movimientos.
     */
    List<ProductValuationReportResponse> getTopProducts(ReportQuery query);

    /**
     * Productos con inventario crítico o por debajo del stock mínimo.
     */
    List<LowStockReportResponse> getLowStock();

    /**
     * Detalle de movimientos filtrado por período, tipo y categoría.
     */
    List<MovementDetailReportResponse> getMovements(ReportQuery query);
}
