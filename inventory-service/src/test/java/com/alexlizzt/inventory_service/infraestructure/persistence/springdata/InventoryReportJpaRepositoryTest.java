package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.StockEntity;

class InventoryReportJpaRepositoryTest extends AbstractJpaTest {

    private static final OffsetDateTime FROM = LocalDate.of(2026, 10, 1)
            .atStartOfDay(ZoneOffset.UTC)
            .toOffsetDateTime();

    private static final OffsetDateTime TO = LocalDate.of(2026, 10, 31)
            .atTime(23, 59, 59)
            .atZone(ZoneOffset.UTC)
            .toOffsetDateTime();

    @Autowired
    private InventoryReportJpaRepository reportJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);

        jdbcTemplate.update("DELETE FROM inventory_movements");
        jdbcTemplate.update("DELETE FROM stocks");
        jdbcTemplate.update("DELETE FROM products");
        jdbcTemplate.update("DELETE FROM categories");

        categoryJpaRepository.saveAll(java.util.List.of(
                new CategoryEntity("category-1", "Laptops", "Equipos portátiles", null, null),
                new CategoryEntity("category-2", "Periféricos", "Accesorios", null, null)));
        categoryJpaRepository.flush();

        productJpaRepository.saveAll(java.util.List.of(
                new ProductEntity("product-1", "category-1", "SKU-001", "Laptop Pro",
                        "Equipo profesional", new BigDecimal("1000.00"), true, null, null),
                new ProductEntity("product-2", "category-2", "SKU-002", "Mouse Inalámbrico",
                        "Periférico", new BigDecimal("50.00"), true, null, null),
                new ProductEntity("product-3", "category-1", "SKU-003", "Monitor 4K",
                        "Monitor profesional", new BigDecimal("300.00"), false, null, null)));
        productJpaRepository.flush();

        stockJpaRepository.saveAll(java.util.List.of(
                new StockEntity("product-1", 10, 5, null),
                new StockEntity("product-2", 2, 10, null),
                new StockEntity("product-3", 0, 5, null)));
        stockJpaRepository.flush();

        insertMovement("movement-1", "product-1", "IN", 5, "Compra", moment(2026, 10, 1, 10, 0));
        insertMovement("movement-2", "product-1", "OUT", 2, "Venta", moment(2026, 10, 2, 10, 0));
        insertMovement("movement-3", "product-2", "IN", 10, "Compra", moment(2026, 10, 2, 15, 0));
    }

    private void insertMovement(
            String id,
            String productId,
            String type,
            int quantity,
            String reason,
            OffsetDateTime createdAt) {

        jdbcTemplate.update(
                "INSERT INTO inventory_movements (id, product_id, type, quantity, reason, user_id, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                id, productId, type, quantity, reason, "user-1", createdAt);
    }

    private static OffsetDateTime moment(int year, int month, int day, int hour, int minute) {
        return LocalDate.of(year, month, day)
                .atTime(hour, minute)
                .atZone(ZoneOffset.UTC)
                .toOffsetDateTime();
    }

    @Test
    void shouldSummarizeInventoryAndPeriodMovements() {
        var summary = reportJpaRepository.summarize(FROM, TO);

        assertThat(summary.getTotalProducts()).isEqualTo(3L);
        assertThat(summary.getActiveProducts()).isEqualTo(2L);
        assertThat(summary.getTotalCategories()).isEqualTo(2L);
        assertThat(summary.getTotalUnits()).isEqualTo(12L);
        assertThat(summary.getLowStockProducts()).isEqualTo(2L);
        assertThat(summary.getOutOfStockProducts()).isEqualTo(1L);
        assertThat(summary.getInventoryValue()).isEqualByComparingTo("10100.00");
        assertThat(summary.getInboundMovements()).isEqualTo(2L);
        assertThat(summary.getOutboundMovements()).isEqualTo(1L);
        assertThat(summary.getInboundUnits()).isEqualTo(15L);
        assertThat(summary.getOutboundUnits()).isEqualTo(2L);
    }

    @Test
    void shouldGroupStockByCategory() {
        var result = reportJpaRepository.findStockByCategory();

        assertThat(result).hasSize(2);

        var laptops = result.get(0);
        assertThat(laptops.getCategoryId()).isEqualTo("category-1");
        assertThat(laptops.getCategoryName()).isEqualTo("Laptops");
        assertThat(laptops.getProductCount()).isEqualTo(2L);
        assertThat(laptops.getTotalUnits()).isEqualTo(10L);
        assertThat(laptops.getTotalValue()).isEqualByComparingTo("10000.00");
        assertThat(laptops.getLowStockCount()).isEqualTo(1L);

        var peripherals = result.get(1);
        assertThat(peripherals.getCategoryId()).isEqualTo("category-2");
        assertThat(peripherals.getProductCount()).isEqualTo(1L);
        assertThat(peripherals.getTotalUnits()).isEqualTo(2L);
        assertThat(peripherals.getTotalValue()).isEqualByComparingTo("100.00");
        assertThat(peripherals.getLowStockCount()).isEqualTo(1L);
    }

    @Test
    void shouldGroupMovementsByDay() {
        var result = reportJpaRepository.findMovementsByPeriod("day", "YYYY-MM-DD", FROM, TO);

        assertThat(result).hasSize(2);

        var firstDay = result.get(0);
        assertThat(firstDay.getPeriod()).isEqualTo("2026-10-01");
        assertThat(firstDay.getInboundUnits()).isEqualTo(5L);
        assertThat(firstDay.getOutboundUnits()).isZero();
        assertThat(firstDay.getInboundMovements()).isEqualTo(1L);
        assertThat(firstDay.getInboundValue()).isEqualByComparingTo("5000.00");

        var secondDay = result.get(1);
        assertThat(secondDay.getPeriod()).isEqualTo("2026-10-02");
        assertThat(secondDay.getInboundUnits()).isEqualTo(10L);
        assertThat(secondDay.getOutboundUnits()).isEqualTo(2L);
        assertThat(secondDay.getOutboundMovements()).isEqualTo(1L);
        assertThat(secondDay.getOutboundValue()).isEqualByComparingTo("2000.00");
    }

    @Test
    void shouldGroupMovementsByMonth() {
        var result = reportJpaRepository.findMovementsByPeriod("month", "YYYY-MM", FROM, TO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPeriod()).isEqualTo("2026-10");
        assertThat(result.get(0).getInboundUnits()).isEqualTo(15L);
        assertThat(result.get(0).getOutboundUnits()).isEqualTo(2L);
    }

    @Test
    void shouldFindTopProductsByValue() {
        var result = reportJpaRepository.findTopProductsByValue(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getSku()).isEqualTo("SKU-001");
        assertThat(result.get(0).getTotalValue()).isEqualByComparingTo("10000.00");
        assertThat(result.get(0).getMovementCount()).isEqualTo(2L);
    }

    @Test
    void shouldFindTopProductsByMovement() {
        var result = reportJpaRepository.findTopProductsByMovement(FROM, TO, 10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSku()).isEqualTo("SKU-001");
        assertThat(result.get(0).getMovementCount()).isEqualTo(2L);
        assertThat(result.get(1).getSku()).isEqualTo("SKU-002");
        assertThat(result.get(1).getMovementCount()).isEqualTo(1L);
    }

    @Test
    void shouldFindLowStockProductsOrderedByShortage() {
        var result = reportJpaRepository.findLowStockProducts();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getSku()).isEqualTo("SKU-002");
        assertThat(result.get(0).getQuantity()).isEqualTo(2L);
        assertThat(result.get(0).getMinStock()).isEqualTo(10L);
        assertThat(result.get(1).getSku()).isEqualTo("SKU-003");
        assertThat(result.get(1).getQuantity()).isZero();
    }

    @Test
    void shouldFindMovementDetailFilteredByTypeAndCategory() {
        var allMovements = reportJpaRepository.findMovements(FROM, TO, null, null, 50);

        assertThat(allMovements).hasSize(3);
        assertThat(allMovements.get(0).getMovementId()).isEqualTo("movement-3");
        assertThat(allMovements.get(0).getSku()).isEqualTo("SKU-002");
        assertThat(allMovements.get(0).getCategoryName()).isEqualTo("Periféricos");
        assertThat(allMovements.get(0).getCreatedAt()).isNotNull();

        var inbound = reportJpaRepository.findMovements(FROM, TO, "IN", null, 50);

        assertThat(inbound).hasSize(2);
        assertThat(inbound).allMatch(movement -> "IN".equals(movement.getType()));

        var byCategory = reportJpaRepository.findMovements(FROM, TO, null, "category-1", 50);

        assertThat(byCategory).hasSize(2);
        assertThat(byCategory).allMatch(movement -> "category-1".equals(movement.getCategoryId()));
    }

    @Test
    void shouldNotReturnMovementsOutsideOfThePeriod() {
        var result = reportJpaRepository.findMovements(
                LocalDate.of(2026, 11, 1).atStartOfDay(ZoneOffset.UTC).toOffsetDateTime(),
                LocalDate.of(2026, 11, 30).atTime(23, 59, 59).atZone(ZoneOffset.UTC).toOffsetDateTime(),
                null, null, 50);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldSupportWeeklyGranularity() {
        var result = reportJpaRepository.findMovementsByPeriod("week", "IYYY-\"W\"IW", FROM, TO);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPeriod()).startsWith("2026-W");
    }

    @Test
    void shouldExposeSupportedGranularities() {
        assertThat(ReportGranularity.values())
                .containsExactly(ReportGranularity.DAY, ReportGranularity.WEEK, ReportGranularity.MONTH);
    }
}
