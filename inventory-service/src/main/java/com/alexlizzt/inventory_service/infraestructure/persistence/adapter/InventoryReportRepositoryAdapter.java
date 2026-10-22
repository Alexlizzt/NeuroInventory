package com.alexlizzt.inventory_service.infraestructure.persistence.adapter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.alexlizzt.inventory_service.domain.model.CategoryStockReport;
import com.alexlizzt.inventory_service.domain.model.InventorySummary;
import com.alexlizzt.inventory_service.domain.model.MovementDetailReport;
import com.alexlizzt.inventory_service.domain.model.MovementPeriodReport;
import com.alexlizzt.inventory_service.domain.model.ProductValuationReport;
import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;
import com.alexlizzt.inventory_service.domain.repository.InventoryReportRepository;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.CategoryStockProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.InventorySummaryProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.MovementDetailProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.MovementPeriodProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.ProductValuationProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.InventoryReportJpaRepository;

/**
 * Adaptador que resuelve las agregaciones de los reportes sobre PostgreSQL.
 */
@Repository
public class InventoryReportRepositoryAdapter implements InventoryReportRepository {

    private static final String WEEK_PATTERN = "IYYY-\"W\"IW";

    private final InventoryReportJpaRepository reportJpaRepository;

    public InventoryReportRepositoryAdapter(InventoryReportJpaRepository reportJpaRepository) {
        this.reportJpaRepository = reportJpaRepository;
    }

    @Override
    public InventorySummary summarize(LocalDateTime from, LocalDateTime to) {
        InventorySummaryProjection projection = reportJpaRepository.summarize(toUtc(from), toUtc(to));

        return new InventorySummary(
                longValue(projection.getTotalProducts()),
                longValue(projection.getActiveProducts()),
                longValue(projection.getTotalCategories()),
                longValue(projection.getTotalUnits()),
                longValue(projection.getLowStockProducts()),
                longValue(projection.getOutOfStockProducts()),
                decimalValue(projection.getInventoryValue()),
                longValue(projection.getInboundMovements()),
                longValue(projection.getOutboundMovements()),
                longValue(projection.getInboundUnits()),
                longValue(projection.getOutboundUnits()));
    }

    @Override
    public List<CategoryStockReport> findStockByCategory() {
        return reportJpaRepository.findStockByCategory()
                .stream()
                .map(this::toCategoryStockReport)
                .toList();
    }

    @Override
    public List<MovementPeriodReport> findMovementsByPeriod(
            ReportGranularity granularity,
            LocalDateTime from,
            LocalDateTime to) {

        ReportGranularity effectiveGranularity = granularity != null ? granularity : ReportGranularity.DAY;

        return reportJpaRepository.findMovementsByPeriod(
                        unitOf(effectiveGranularity),
                        patternOf(effectiveGranularity),
                        toUtc(from),
                        toUtc(to))
                .stream()
                .map(this::toMovementPeriodReport)
                .toList();
    }

    @Override
    public List<ProductValuationReport> findTopProducts(
            ReportMetric metric,
            int limit,
            LocalDateTime from,
            LocalDateTime to) {

        List<ProductValuationProjection> projections = metric == ReportMetric.MOVEMENT
                ? reportJpaRepository.findTopProductsByMovement(toUtc(from), toUtc(to), limit)
                : reportJpaRepository.findTopProductsByValue(limit);

        return projections.stream()
                .map(this::toProductValuationReport)
                .toList();
    }

    @Override
    public List<ProductValuationReport> findLowStock() {
        return reportJpaRepository.findLowStockProducts()
                .stream()
                .map(this::toProductValuationReport)
                .toList();
    }

    @Override
    public List<MovementDetailReport> findMovements(
            LocalDateTime from,
            LocalDateTime to,
            String type,
            String categoryId,
            int limit) {

        return reportJpaRepository.findMovements(toUtc(from), toUtc(to), type, categoryId, limit)
                .stream()
                .map(this::toMovementDetailReport)
                .toList();
    }

    private CategoryStockReport toCategoryStockReport(CategoryStockProjection projection) {
        return new CategoryStockReport(
                projection.getCategoryId(),
                projection.getCategoryName(),
                longValue(projection.getProductCount()),
                longValue(projection.getTotalUnits()),
                decimalValue(projection.getTotalValue()),
                longValue(projection.getLowStockCount()));
    }

    private MovementPeriodReport toMovementPeriodReport(MovementPeriodProjection projection) {
        return new MovementPeriodReport(
                projection.getPeriod(),
                longValue(projection.getInboundUnits()),
                longValue(projection.getOutboundUnits()),
                longValue(projection.getAdjustmentUnits()),
                longValue(projection.getInboundMovements()),
                longValue(projection.getOutboundMovements()),
                decimalValue(projection.getInboundValue()),
                decimalValue(projection.getOutboundValue()));
    }

    private ProductValuationReport toProductValuationReport(ProductValuationProjection projection) {
        return new ProductValuationReport(
                projection.getProductId(),
                projection.getSku(),
                projection.getProductName(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                longValue(projection.getQuantity()),
                longValue(projection.getMinStock()),
                decimalValue(projection.getUnitPrice()),
                decimalValue(projection.getTotalValue()),
                longValue(projection.getMovementCount()));
    }

    private MovementDetailReport toMovementDetailReport(MovementDetailProjection projection) {
        return new MovementDetailReport(
                projection.getMovementId(),
                projection.getProductId(),
                projection.getSku(),
                projection.getProductName(),
                projection.getCategoryId(),
                projection.getCategoryName(),
                projection.getType(),
                longValue(projection.getQuantity()),
                decimalValue(projection.getUnitPrice()),
                decimalValue(projection.getTotalValue()),
                projection.getReason(),
                projection.getUserId(),
                projection.getCreatedAt());
    }

    private static String unitOf(ReportGranularity granularity) {
        return switch (granularity) {
            case DAY -> "day";
            case WEEK -> "week";
            case MONTH -> "month";
        };
    }

    private static String patternOf(ReportGranularity granularity) {
        return switch (granularity) {
            case DAY -> "YYYY-MM-DD";
            case WEEK -> WEEK_PATTERN;
            case MONTH -> "YYYY-MM";
        };
    }

    /**
     * El dominio trabaja con instantes en UTC, por lo que los límites del
     * período se convierten a offsets absolutos antes de consultarse.
     */
    private static OffsetDateTime toUtc(LocalDateTime value) {
        return value.atOffset(ZoneOffset.UTC);
    }

    private static long longValue(Long value) {
        return value != null ? value : 0L;
    }

    private static BigDecimal decimalValue(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
