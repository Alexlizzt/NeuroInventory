package com.alexlizzt.inventory_service.application.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;

/**
 * Parámetros de filtrado de los reportes de inventario.
 *
 * @param from        fecha inicial del período (inclusive)
 * @param to          fecha final del período (inclusive)
 * @param granularity granularidad de agrupación de la serie temporal
 * @param metric      métrica de ordenamiento del reporte de productos destacados
 * @param type        tipo de movimiento a filtrar (IN, OUT, ADJUSTMENT) o {@code null}
 * @param categoryId  categoría a filtrar o {@code null}
 * @param limit       cantidad máxima de registros a devolver
 */
public record ReportQuery(
        LocalDateTime from,
        LocalDateTime to,
        ReportGranularity granularity,
        ReportMetric metric,
        String type,
        String categoryId,
        int limit) {

    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 200;
    public static final int DEFAULT_PERIOD_DAYS = 30;

    /**
     * Construye la consulta aplicando los valores por defecto y normalizando
     * los filtros opcionales.
     */
    public static ReportQuery of(
            LocalDate from,
            LocalDate to,
            ReportGranularity granularity,
            ReportMetric metric,
            String type,
            String categoryId,
            Integer limit) {

        LocalDate defaultTo = LocalDate.now(ZoneOffset.UTC);
        LocalDate defaultFrom = defaultTo.minusDays(DEFAULT_PERIOD_DAYS);

        LocalDateTime resolvedFrom = toUtcStartOfDay(from != null ? from : defaultFrom);
        LocalDateTime resolvedTo = toUtcEndOfDay(to != null ? to : defaultTo);

        if (resolvedFrom.isAfter(resolvedTo)) {
            LocalDateTime swap = resolvedFrom;
            resolvedFrom = resolvedTo;
            resolvedTo = swap;
        }

        int resolvedLimit = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);

        return new ReportQuery(
                resolvedFrom,
                resolvedTo,
                granularity != null ? granularity : ReportGranularity.DAY,
                metric != null ? metric : ReportMetric.VALUE,
                normalizeUpperCase(type),
                normalize(categoryId),
                resolvedLimit);
    }

    /**
     * Período por defecto: los últimos 30 días hasta hoy (UTC).
     */
    public static ReportQuery defaultPeriod() {
        return of(null, null, ReportGranularity.DAY, ReportMetric.VALUE, null, null, null);
    }

    private static LocalDateTime toUtcStartOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MIN);
    }

    private static LocalDateTime toUtcEndOfDay(LocalDate date) {
        return LocalDateTime.of(date, LocalTime.MAX).truncatedTo(ChronoUnit.MILLIS);
    }

    private static String normalizeUpperCase(String value) {
        String normalized = normalize(value);
        return normalized != null ? normalized.toUpperCase() : null;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
