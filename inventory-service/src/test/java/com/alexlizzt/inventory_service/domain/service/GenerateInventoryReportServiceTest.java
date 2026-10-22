package com.alexlizzt.inventory_service.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryReportDtoMapper;
import com.alexlizzt.inventory_service.application.query.ReportQuery;
import com.alexlizzt.inventory_service.domain.model.CategoryStockReport;
import com.alexlizzt.inventory_service.domain.model.InventorySummary;
import com.alexlizzt.inventory_service.domain.model.MovementDetailReport;
import com.alexlizzt.inventory_service.domain.model.MovementPeriodReport;
import com.alexlizzt.inventory_service.domain.model.ProductValuationReport;
import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;
import com.alexlizzt.inventory_service.domain.repository.InventoryReportRepository;

@ExtendWith(MockitoExtension.class)
class GenerateInventoryReportServiceTest {

    @Mock
    private InventoryReportRepository reportRepository;

    @Mock
    private InventoryReportDtoMapper reportMapper;

    @InjectMocks
    private GenerateInventoryReportService generateInventoryReportService;

    @Test
    @DisplayName("Debe retornar el resumen del dashboard mapeado con el período analizado")
    void shouldReturnDashboardSummarySuccessfully() {
        // Arrange
        var query = ReportQuery.defaultPeriod();
        var summary = new InventorySummary(
                10, 9, 2, 120, 3, 1, new BigDecimal("15000.00"),
                5, 4, 50, 30);

        var expected = new DashboardSummaryResponse(
                10, 9, 2, 120, 3, 1, new BigDecimal("15000.00"),
                5, 4, 50, 30, 20, query.from(), query.to(), LocalDateTime.now());

        when(reportRepository.summarize(query.from(), query.to())).thenReturn(summary);
        when(reportMapper.toSummaryResponse(summary, query)).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getDashboardSummary(query);

        // Assert
        assertThat(result).isEqualTo(expected);
        assertThat(result.netUnits()).isEqualTo(20);

        verify(reportRepository).summarize(query.from(), query.to());
    }

    @Test
    @DisplayName("Debe calcular el valor total para obtener los porcentajes por categoría")
    void shouldReturnStockByCategoryUsingTotalValue() {
        // Arrange
        var firstCategory = new CategoryStockReport(
                "cat-1", "Laptops", 5, 40, new BigDecimal("1000.00"), 2);
        var secondCategory = new CategoryStockReport(
                "cat-2", "Periféricos", 3, 10, new BigDecimal("1000.00"), 0);

        var expected = List.of(
                new CategoryStockReportResponse("cat-1", "Laptops", 5, 40, new BigDecimal("1000.00"), 50d, 2),
                new CategoryStockReportResponse("cat-2", "Periféricos", 3, 10, new BigDecimal("1000.00"), 50d, 0));

        when(reportRepository.findStockByCategory()).thenReturn(List.of(firstCategory, secondCategory));
        when(reportMapper.toCategoryResponses(any(), any())).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getStockByCategory();

        // Assert
        assertThat(result).isEqualTo(expected);
        verify(reportMapper).toCategoryResponses(any(), eq(new BigDecimal("2000.00")));
    }

    @Test
    @DisplayName("Debe retornar la serie temporal de movimientos")
    void shouldReturnMovementsByPeriodSuccessfully() {
        // Arrange
        var query = ReportQuery.defaultPeriod();
        var period = new MovementPeriodReport(
                "2026-10-01", 10, 4, 1, 2, 1, new BigDecimal("100.00"), new BigDecimal("40.00"));
        var expected = new MovementPeriodReportResponse(
                "2026-10-01", 10, 4, 1, 6, 2, 1,
                new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"));

        when(reportRepository.findMovementsByPeriod(ReportGranularity.DAY, query.from(), query.to()))
                .thenReturn(List.of(period));
        when(reportMapper.toPeriodResponse(period)).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getMovementsByPeriod(query);

        // Assert
        assertThat(result).containsExactly(expected);
        assertThat(result.get(0).netUnits()).isEqualTo(6);
    }

    @Test
    @DisplayName("Debe retornar los productos destacados según la métrica solicitada")
    void shouldReturnTopProductsSuccessfully() {
        // Arrange
        var query = ReportQuery.defaultPeriod();
        var report = new ProductValuationReport(
                "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", 10, 5,
                new BigDecimal("100.00"), new BigDecimal("1000.00"), 3);
        var expected = new ProductValuationReportResponse(
                "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", 10, 5,
                new BigDecimal("100.00"), new BigDecimal("1000.00"), 3);

        when(reportRepository.findTopProducts(ReportMetric.VALUE, query.limit(), query.from(), query.to()))
                .thenReturn(List.of(report));
        when(reportMapper.toProductResponse(report)).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getTopProducts(query);

        // Assert
        assertThat(result).containsExactly(expected);
        verify(reportRepository).findTopProducts(ReportMetric.VALUE, query.limit(), query.from(), query.to());
    }

    @Test
    @DisplayName("Debe retornar el reporte avanzado de stock bajo")
    void shouldReturnLowStockReportSuccessfully() {
        // Arrange
        var report = new ProductValuationReport(
                "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", 2, 5,
                new BigDecimal("100.00"), new BigDecimal("200.00"), 1);
        var expected = new LowStockReportResponse(
                "prod-1", "SKU-1", "Laptop", "Laptops", 2, 5, 3, 40d,
                new BigDecimal("100.00"), new BigDecimal("200.00"),
                new BigDecimal("300.00"), "WARNING");

        when(reportRepository.findLowStock()).thenReturn(List.of(report));
        when(reportMapper.toLowStockResponse(report)).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getLowStock();

        // Assert
        assertThat(result).containsExactly(expected);
    }

    @Test
    @DisplayName("Debe retornar el detalle de movimientos con los filtros aplicados")
    void shouldReturnMovementsDetailSuccessfully() {
        // Arrange
        var query = ReportQuery.defaultPeriod();
        var report = new MovementDetailReport(
                "mov-1", "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", "IN",
                5, new BigDecimal("100.00"), new BigDecimal("500.00"),
                "Compra", "user-1", LocalDateTime.now());
        var expected = new MovementDetailReportResponse(
                "mov-1", "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", "IN",
                5, new BigDecimal("100.00"), new BigDecimal("500.00"),
                "Compra", "user-1", report.createdAt());

        when(reportRepository.findMovements(query.from(), query.to(), query.type(), query.categoryId(), query.limit()))
                .thenReturn(List.of(report));
        when(reportMapper.toMovementResponse(report)).thenReturn(expected);

        // Act
        var result = generateInventoryReportService.getMovements(query);

        // Assert
        assertThat(result).containsExactly(expected);
        verify(reportRepository).findMovements(
                query.from(), query.to(), query.type(), query.categoryId(), query.limit());
    }

    @Test
    @DisplayName("Debe retornar listas vacías cuando no existen datos para el reporte")
    void shouldReturnEmptyListsWhenNoDataAvailable() {
        // Arrange
        var query = ReportQuery.defaultPeriod();
        when(reportRepository.findStockByCategory()).thenReturn(List.of());
        when(reportRepository.findLowStock()).thenReturn(List.of());
        when(reportRepository.findMovementsByPeriod(any(), any(), any())).thenReturn(List.of());
        when(reportRepository.findTopProducts(any(), anyInt(), any(), any())).thenReturn(List.of());
        when(reportRepository.findMovements(any(), any(), any(), any(), anyInt())).thenReturn(List.of());

        // Act & Assert
        assertThat(generateInventoryReportService.getStockByCategory()).isEmpty();
        assertThat(generateInventoryReportService.getLowStock()).isEmpty();
        assertThat(generateInventoryReportService.getMovementsByPeriod(query)).isEmpty();
        assertThat(generateInventoryReportService.getTopProducts(query)).isEmpty();
        assertThat(generateInventoryReportService.getMovements(query)).isEmpty();
    }
}
