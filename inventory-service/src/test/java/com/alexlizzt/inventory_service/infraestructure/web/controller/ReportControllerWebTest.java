package com.alexlizzt.inventory_service.infraestructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.domain.service.GenerateInventoryReportService;
import com.alexlizzt.inventory_service.infraestructure.web.exception.GlobalExceptionHandler;

@WebMvcTest(
    controllers = ReportController.class,
    properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration," +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.JwtDecoderAutoConfiguration"
    }
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class ReportControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateInventoryReportService reportService;

    @Test
    void shouldReturnDashboardSummary() throws Exception {
        var response = new DashboardSummaryResponse(
                10, 9, 2, 120, 3, 1, new BigDecimal("15000.00"),
                5, 4, 50, 30, 20,
                LocalDateTime.of(2026, 9, 21, 0, 0),
                LocalDateTime.of(2026, 10, 21, 23, 59),
                LocalDateTime.now());

        when(reportService.getDashboardSummary(any())).thenReturn(response);

        mockMvc.perform(get("/reports/dashboard").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(10))
                .andExpect(jsonPath("$.inventoryValue").value(15000.00))
                .andExpect(jsonPath("$.netUnits").value(20));

        verify(reportService).getDashboardSummary(any());
    }

    @Test
    void shouldReturnValuationByCategory() throws Exception {
        var response = new CategoryStockReportResponse(
                "cat-1", "Laptops", 5, 40, new BigDecimal("1000.00"), 62.5d, 2);

        when(reportService.getStockByCategory()).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/valuation/categories").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Laptops"))
                .andExpect(jsonPath("$[0].percentage").value(62.5));
    }

    @Test
    void shouldReturnMovementsTimeseries() throws Exception {
        var response = new MovementPeriodReportResponse(
                "2026-10-01", 10, 4, 1, 6, 2, 1,
                new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"));

        when(reportService.getMovementsByPeriod(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/movements/timeseries")
                        .with(user("user").roles("USER"))
                        .param("granularity", "MONTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].period").value("2026-10-01"))
                .andExpect(jsonPath("$[0].netUnits").value(6));
    }

    @Test
    void shouldReturnMovementsDetail() throws Exception {
        var response = new MovementDetailReportResponse(
                "mov-1", "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", "IN",
                5, new BigDecimal("100.00"), new BigDecimal("500.00"),
                "Compra", "user-1", LocalDateTime.now());

        when(reportService.getMovements(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/movements/detail")
                        .with(user("user").roles("USER"))
                        .param("type", "IN")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$[0].type").value("IN"));
    }

    @Test
    void shouldReturnTopProducts() throws Exception {
        var response = new ProductValuationReportResponse(
                "prod-1", "SKU-1", "Laptop", "cat-1", "Laptops", 10, 5,
                new BigDecimal("100.00"), new BigDecimal("1000.00"), 3);

        when(reportService.getTopProducts(any())).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/products/top")
                        .with(user("admin").roles("ADMIN"))
                        .param("metric", "MOVEMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$[0].movementCount").value(3));
    }

    @Test
    void shouldReturnLowStockReport() throws Exception {
        var response = new LowStockReportResponse(
                "prod-1", "SKU-1", "Laptop", "Laptops", 2, 5, 3, 40d,
                new BigDecimal("100.00"), new BigDecimal("200.00"),
                new BigDecimal("300.00"), "WARNING");

        when(reportService.getLowStock()).thenReturn(List.of(response));

        mockMvc.perform(get("/reports/stock/low").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].severity").value("WARNING"))
                .andExpect(jsonPath("$[0].shortage").value(3));
    }

    @Test
    void shouldReturnForbiddenWhenRequestingReportsWithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/reports/dashboard").with(user("guest").roles("GUEST")))
                .andExpect(status().isForbidden());
    }
}
