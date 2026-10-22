package com.alexlizzt.inventory_service.application.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.application.query.ReportQuery;
import com.alexlizzt.inventory_service.domain.model.CategoryStockReport;
import com.alexlizzt.inventory_service.domain.model.InventorySummary;
import com.alexlizzt.inventory_service.domain.model.MovementDetailReport;
import com.alexlizzt.inventory_service.domain.model.MovementPeriodReport;
import com.alexlizzt.inventory_service.domain.model.ProductValuationReport;

@Mapper(componentModel = "spring")
public interface InventoryReportDtoMapper {

    @Mapping(target = "netUnits", expression = "java(report.netUnits())")
    @Mapping(target = "netValue", expression = "java(report.netValue())")
    MovementPeriodReportResponse toPeriodResponse(MovementPeriodReport report);

    ProductValuationReportResponse toProductResponse(ProductValuationReport report);

    MovementDetailReportResponse toMovementResponse(MovementDetailReport report);

    /**
     * Mapea el resumen del inventario incorporando el período analizado.
     */
    default DashboardSummaryResponse toSummaryResponse(InventorySummary summary, ReportQuery query) {
        return new DashboardSummaryResponse(
                summary.totalProducts(),
                summary.activeProducts(),
                summary.totalCategories(),
                summary.totalUnits(),
                summary.lowStockProducts(),
                summary.outOfStockProducts(),
                summary.inventoryValue(),
                summary.inboundMovements(),
                summary.outboundMovements(),
                summary.inboundUnits(),
                summary.outboundUnits(),
                summary.netUnits(),
                query.from(),
                query.to(),
                LocalDateTime.now());
    }

    /**
     * Mapea el inventario por categoría calculando su participación porcentual
     * sobre el valor total del inventario.
     */
    default List<CategoryStockReportResponse> toCategoryResponses(
            List<CategoryStockReport> reports,
            BigDecimal totalInventoryValue) {

        BigDecimal total = totalInventoryValue != null ? totalInventoryValue : BigDecimal.ZERO;

        return reports.stream()
                .map(report -> new CategoryStockReportResponse(
                        report.categoryId(),
                        report.categoryName(),
                        report.productCount(),
                        report.totalUnits(),
                        report.totalValue(),
                        percentage(report.totalValue(), total),
                        report.lowStockCount()))
                .toList();
    }

    /**
     * Mapea el reporte de inventario crítico calculando el faltante, la
     * cobertura y el costo estimado de reposición.
     */
    default LowStockReportResponse toLowStockResponse(ProductValuationReport report) {
        long shortage = report.shortage();
        BigDecimal unitPrice = report.unitPrice() != null ? report.unitPrice() : BigDecimal.ZERO;

        return new LowStockReportResponse(
                report.productId(),
                report.sku(),
                report.productName(),
                report.categoryName(),
                report.quantity(),
                report.minStock(),
                shortage,
                coveragePercentage(report.quantity(), report.minStock()),
                unitPrice,
                report.totalValue(),
                unitPrice.multiply(BigDecimal.valueOf(shortage)),
                report.severity().name());
    }

    private static double percentage(BigDecimal value, BigDecimal total) {
        if (value == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return 0d;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static double coveragePercentage(long quantity, long minStock) {
        if (minStock <= 0) {
            return quantity > 0 ? 100d : 0d;
        }
        return BigDecimal.valueOf(quantity)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(minStock), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
