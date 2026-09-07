package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.domain.exception.StockNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStockByProductServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private GetStockByProductService getStockByProductService;

    @Test
    @DisplayName("Debe retornar la respuesta de stock correctamente cuando el producto existe")
    void shouldReturnStockSuccessfullyWhenProductExists() {
        // Arrange
        String productId = "prod-123";
        LocalDateTime now = LocalDateTime.now();
        
        Stock stock = Stock.builder()
                .productId(productId)
                .quantity(10)
                .minStock(5)
                .updatedAt(now)
                .build();

        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        // Act
        StockResponse response = getStockByProductService.execute(productId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(response.minStock()).isEqualTo(5);
        assertThat(response.isLowStock()).isFalse();
        assertThat(response.updatedAt()).isEqualTo(now);

        // Verify interactions
        verify(stockRepository).findByProductId(productId);
    }

    @Test
    @DisplayName("Debe lanzar StockNotFoundException si el stock no existe para el producto")
    void shouldThrowExceptionWhenStockNotFound() {
        // Arrange
        String productId = "prod-999";

        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getStockByProductService.execute(productId))
                .isInstanceOf(StockNotFoundException.class);

        // Verify interactions
        verify(stockRepository).findByProductId(productId);
    }
}