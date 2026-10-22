package com.alexlizzt.inventory_service.application.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.alexlizzt.inventory_service.domain.model.ReportGranularity;
import com.alexlizzt.inventory_service.domain.model.ReportMetric;

class ReportQueryTest {

    @Test
    @DisplayName("Debe aplicar el período y granularidad por defecto cuando no se informan")
    void shouldApplyDefaultValues() {
        var query = ReportQuery.defaultPeriod();

        assertThat(query.granularity()).isEqualTo(ReportGranularity.DAY);
        assertThat(query.metric()).isEqualTo(ReportMetric.VALUE);
        assertThat(query.limit()).isEqualTo(ReportQuery.DEFAULT_LIMIT);
        assertThat(query.type()).isNull();
        assertThat(query.categoryId()).isNull();
        assertThat(query.to()).isAfter(query.from());
    }

    @Test
    @DisplayName("Debe normalizar el tipo de movimiento a mayúsculas")
    void shouldNormalizeMovementType() {
        var query = ReportQuery.of(
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 21),
                ReportGranularity.MONTH,
                ReportMetric.MOVEMENT,
                " in ",
                null,
                25);

        assertThat(query.type()).isEqualTo("IN");
        assertThat(query.categoryId()).isNull();
        assertThat(query.granularity()).isEqualTo(ReportGranularity.MONTH);
        assertThat(query.metric()).isEqualTo(ReportMetric.MOVEMENT);
        assertThat(query.limit()).isEqualTo(25);
    }

    @Test
    @DisplayName("Debe conservar el identificador de categoría sin modificar")
    void shouldKeepCategoryIdCase() {
        var query = ReportQuery.of(
                null, null, null, null, null, " cat-123 ", null);

        assertThat(query.categoryId()).isEqualTo("cat-123");
    }

    @Test
    @DisplayName("Debe intercambiar las fechas cuando el rango es inverso")
    void shouldSwapDatesWhenRangeIsInverted() {
        var query = ReportQuery.of(
                LocalDate.of(2026, 10, 21),
                LocalDate.of(2026, 10, 1),
                null, null, null, null, null);

        assertThat(query.from()).isBefore(query.to());
        assertThat(query.from().toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(query.to().toLocalDate()).isEqualTo(LocalDate.of(2026, 10, 21));
    }

    @Test
    @DisplayName("Debe limitar la cantidad máxima de registros solicitados")
    void shouldCapLimitToMaximumAllowed() {
        var query = ReportQuery.of(null, null, null, null, null, null, 5000);

        assertThat(query.limit()).isEqualTo(ReportQuery.MAX_LIMIT);
    }
}
