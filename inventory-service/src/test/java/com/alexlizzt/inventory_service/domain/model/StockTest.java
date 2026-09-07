package com.alexlizzt.inventory_service.domain.model;

import com.alexlizzt.inventory_service.domain.exception.InsufficientStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StockTest {

    @Test
    @DisplayName("Debe retornar falso si quantity o minStock son nulos")
    void shouldReturnFalseWhenQuantityOrMinStockIsNull() {
        Stock stock1 = Stock.builder().productId("1").quantity(5).minStock(null).build();
        Stock stock2 = Stock.builder().productId("1").quantity(null).minStock(5).build();
        Stock stock3 = Stock.builder().productId("1").quantity(null).minStock(null).build();

        assertThat(stock1.isLowStock()).isFalse();
        assertThat(stock2.isLowStock()).isFalse();
        assertThat(stock3.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("Debe retornar verdadero si quantity es menor o igual a minStock")
    void shouldReturnTrueWhenLowStock() {
        Stock stockEqual = Stock.builder().productId("1").quantity(5).minStock(5).build();
        Stock stockLess = Stock.builder().productId("1").quantity(3).minStock(5).build();

        assertThat(stockEqual.isLowStock()).isTrue();
        assertThat(stockLess.isLowStock()).isTrue();
    }

    @Test
    @DisplayName("Debe retornar falso si quantity es mayor a minStock")
    void shouldReturnFalseWhenStockIsSufficient() {
        Stock stock = Stock.builder().productId("1").quantity(10).minStock(5).build();
        assertThat(stock.isLowStock()).isFalse();
    }

    @Test
    @DisplayName("Debe incrementar la cantidad y actualizar la fecha de modificación correctamente")
    void shouldAddQuantitySuccessfully() {
        LocalDateTime oldDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        Stock stock = Stock.builder()
                .productId("prod-1")
                .quantity(10)
                .updatedAt(oldDate)
                .build();

        stock.addQuantity(5);

        assertThat(stock.getQuantity()).isEqualTo(15);
        assertThat(stock.getUpdatedAt()).isAfter(oldDate);
    }

    @Test
    @DisplayName("Debe tratar la cantidad como cero si es nula al incrementar")
    void shouldTreatQuantityAsZeroWhenNullOnAdd() {
        Stock stock = Stock.builder()
                .productId("prod-1")
                .quantity(null)
                .build();

        stock.addQuantity(10);

        assertThat(stock.getQuantity()).isEqualTo(10);
        assertThat(stock.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Debe lanzar InsufficientStockException al intentar añadir una cantidad menor o igual a cero")
    void shouldThrowExceptionWhenAddingInvalidAmount() {
        Stock stock = Stock.builder().productId("prod-1").quantity(10).build();

        assertThatThrownBy(() -> stock.addQuantity(0))
                .isInstanceOf(InsufficientStockException.class);

        assertThatThrownBy(() -> stock.addQuantity(-5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Debe disminuir la cantidad y actualizar la fecha de modificación correctamente")
    void shouldRemoveQuantitySuccessfully() {
        LocalDateTime oldDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        Stock stock = Stock.builder()
                .productId("prod-1")
                .quantity(20)
                .updatedAt(oldDate)
                .build();

        stock.removeQuantity(5);

        assertThat(stock.getQuantity()).isEqualTo(15);
        assertThat(stock.getUpdatedAt()).isAfter(oldDate);
    }

    @Test
    @DisplayName("Debe lanzar InsufficientStockException al intentar retirar más cantidad de la disponible")
    void shouldThrowExceptionWhenRemovingMoreThanAvailable() {
        Stock stock = Stock.builder().productId("prod-1").quantity(10).build();

        assertThatThrownBy(() -> stock.removeQuantity(15))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Debe lanzar InsufficientStockException al intentar retirar una cantidad menor o igual a cero")
    void shouldThrowExceptionWhenRemovingInvalidAmount() {
        Stock stock = Stock.builder().productId("prod-1").quantity(10).build();

        assertThatThrownBy(() -> stock.removeQuantity(0))
                .isInstanceOf(InsufficientStockException.class);

        assertThatThrownBy(() -> stock.removeQuantity(-2))
                .isInstanceOf(InsufficientStockException.class);
    }
}