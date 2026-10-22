package com.alexlizzt.inventory_service.domain.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryReportDtoMapper;
import com.alexlizzt.inventory_service.application.query.ReportQuery;
import com.alexlizzt.inventory_service.application.usecase.GenerateInventoryReportUseCase;
import com.alexlizzt.inventory_service.domain.model.CategoryStockReport;
import com.alexlizzt.inventory_service.domain.repository.InventoryReportRepository;

/**
 * Servicio de dominio que orquesta la generación de los reportes de inventario.
 */
@Service
public class GenerateInventoryReportService implements GenerateInventoryReportUseCase {

    private final InventoryReportRepository reportRepository;
    private final InventoryReportDtoMapper reportMapper;

    public GenerateInventoryReportService(
            InventoryReportRepository reportRepository,
            InventoryReportDtoMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(ReportQuery query) {
        var summary = reportRepository.summarize(query.from(), query.to());
        return reportMapper.toSummaryResponse(summary, query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStockReportResponse> getStockByCategory() {
        List<CategoryStockReport> reports = reportRepository.findStockByCategory();
        BigDecimal totalValue = reports.stream()
                .map(CategoryStockReport::totalValue)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return reportMapper.toCategoryResponses(reports, totalValue);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementPeriodReportResponse> getMovementsByPeriod(ReportQuery query) {
        return reportRepository.findMovementsByPeriod(query.granularity(), query.from(), query.to())
                .stream()
                .map(reportMapper::toPeriodResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductValuationReportResponse> getTopProducts(ReportQuery query) {
        return reportRepository.findTopProducts(query.metric(), query.limit(), query.from(), query.to())
                .stream()
                .map(reportMapper::toProductResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LowStockReportResponse> getLowStock() {
        return reportRepository.findLowStock()
                .stream()
                .map(reportMapper::toLowStockResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovementDetailReportResponse> getMovements(ReportQuery query) {
        return reportRepository
                .findMovements(query.from(), query.to(), query.type(), query.categoryId(), query.limit())
                .stream()
                .map(reportMapper::toMovementResponse)
                .toList();
    }
}
