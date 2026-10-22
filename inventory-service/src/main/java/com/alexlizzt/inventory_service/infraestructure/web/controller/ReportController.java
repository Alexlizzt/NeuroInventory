package com.alexlizzt.inventory_service.infraestructure.web.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alexlizzt.inventory_service.application.dto.response.CategoryStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.DashboardSummaryResponse;
import com.alexlizzt.inventory_service.application.dto.response.LowStockReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementDetailReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.MovementPeriodReportResponse;
import com.alexlizzt.inventory_service.application.dto.response.ProductValuationReportResponse;
import com.alexlizzt.inventory_service.application.query.ReportQuery;
import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;
import com.alexlizzt.inventory_service.domain.service.GenerateInventoryReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reportes", description = "Endpoints de analítica y reportes avanzados de inventario")
public class ReportController {

    private final GenerateInventoryReportService reportService;

    public ReportController(GenerateInventoryReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener el resumen ejecutivo del inventario",
        description = "Devuelve los indicadores clave (KPI) del inventario junto con el flujo de movimientos del período seleccionado. Si no se indica período, se analizan los últimos 30 días."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resumen del inventario generado exitosamente",
            content = @Content(schema = @Schema(implementation = DashboardSummaryResponse.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(
            @Parameter(description = "Fecha inicial del período (YYYY-MM-DD)", example = "2026-09-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha final del período (YYYY-MM-DD)", example = "2026-10-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        var query = ReportQuery.of(from, to, ReportGranularity.DAY, ReportMetric.VALUE, null, null, null);
        return ResponseEntity.ok(reportService.getDashboardSummary(query));
    }

    @GetMapping("/valuation/categories")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener la valoración del inventario por categoría",
        description = "Agrupa los productos por categoría indicando unidades, valor monetario, participación porcentual y cantidad de productos con stock crítico."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Valoración por categoría generada exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoryStockReportResponse.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<CategoryStockReportResponse>> getValuationByCategory() {
        return ResponseEntity.ok(reportService.getStockByCategory());
    }

    @GetMapping("/movements/timeseries")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener la serie temporal de movimientos",
        description = "Agrupa los movimientos de inventario por día, semana o mes, calculando unidades y valores de entrada, salida y ajuste."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Serie temporal generada exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MovementPeriodReportResponse.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<MovementPeriodReportResponse>> getMovementsTimeseries(
            @Parameter(description = "Granularidad de la agrupación", example = "DAY")
            @RequestParam(defaultValue = "DAY") ReportGranularity granularity,
            @Parameter(description = "Fecha inicial del período (YYYY-MM-DD)", example = "2026-09-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha final del período (YYYY-MM-DD)", example = "2026-10-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        var query = ReportQuery.of(from, to, granularity, ReportMetric.VALUE, null, null, null);
        return ResponseEntity.ok(reportService.getMovementsByPeriod(query));
    }

    @GetMapping("/movements/detail")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener el detalle de movimientos de inventario",
        description = "Devuelve el kardex detallado de movimientos con los datos del producto y su categoría, filtrable por período, tipo de movimiento y categoría."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle de movimientos generado exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MovementDetailReportResponse.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<MovementDetailReportResponse>> getMovementsDetail(
            @Parameter(description = "Fecha inicial del período (YYYY-MM-DD)", example = "2026-09-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha final del período (YYYY-MM-DD)", example = "2026-10-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Tipo de movimiento (IN, OUT, ADJUSTMENT)", example = "IN")
            @RequestParam(required = false) String type,
            @Parameter(description = "Identificador de la categoría", example = "cat-123456")
            @RequestParam(required = false) String categoryId,
            @Parameter(description = "Cantidad máxima de registros", example = "50")
            @RequestParam(required = false) Integer limit) {

        var query = ReportQuery.of(from, to, ReportGranularity.DAY, ReportMetric.VALUE, type, categoryId, limit);
        return ResponseEntity.ok(reportService.getMovements(query));
    }

    @GetMapping("/products/top")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener los productos destacados",
        description = "Lista los productos con mayor valor en inventario o con mayor cantidad de movimientos registrados en el período."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Productos destacados generados exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductValuationReportResponse.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<ProductValuationReportResponse>> getTopProducts(
            @Parameter(description = "Métrica de ordenamiento (VALUE o MOVEMENT)", example = "VALUE")
            @RequestParam(defaultValue = "VALUE") ReportMetric metric,
            @Parameter(description = "Cantidad de productos a devolver", example = "10")
            @RequestParam(required = false) Integer limit,
            @Parameter(description = "Fecha inicial del período (YYYY-MM-DD)", example = "2026-09-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Fecha final del período (YYYY-MM-DD)", example = "2026-10-21")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        var query = ReportQuery.of(from, to, ReportGranularity.DAY, metric, null, null, limit);
        return ResponseEntity.ok(reportService.getTopProducts(query));
    }

    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener el reporte avanzado de stock bajo",
        description = "Detalla los productos en o por debajo del stock mínimo, incluyendo el faltante, la cobertura, la severidad y el costo estimado de reposición."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reporte de stock bajo generado exitosamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LowStockReportResponse.class)))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<LowStockReportResponse>> getLowStockReport() {
        return ResponseEntity.ok(reportService.getLowStock());
    }
}
