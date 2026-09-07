package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.application.mapper.StockDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetLowStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockDtoMapper stockDtoMapper;

    @InjectMocks
    private GetLowStockService getLowStockService;

    @Test
    @DisplayName("Debe retornar una página paginada de stock bajo mapeada exitosamente")
    void shouldReturnPagedLowStockSuccessfully() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        Stock stock = Stock.builder()
                .productId("prod-1")
                .quantity(2)
                .minStock(5)
                .build();

        PageResult<Stock> domainPage = new PageResult<>(
                List.of(stock),
                0,
                10,
                1L,
                1
        );

        when(stockRepository.findLowStockPaged(pageQuery)).thenReturn(domainPage);

        StockResponse stockResponse = new StockResponse("prod-1", 2, 5,false, null);
        when(stockDtoMapper.toResponse(stock)).thenReturn(stockResponse);

        // Act
        PageResult<StockResponse> result = getLowStockService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).productId()).isEqualTo("prod-1");
        assertThat(result.content().get(0).quantity()).isEqualTo(2);
        assertThat(result.pageNumber()).isZero();
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);

        // Verify interactions
        verify(stockRepository).findLowStockPaged(pageQuery);
        verify(stockDtoMapper).toResponse(stock);
    }

    @Test
    @DisplayName("Debe retornar una página vacía si no hay productos con stock bajo")
    void shouldReturnEmptyPageWhenNoLowStockExists() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        PageResult<Stock> emptyPage = new PageResult<>(
                List.of(),
                0,
                10,
                0L,
                0
        );

        when(stockRepository.findLowStockPaged(pageQuery)).thenReturn(emptyPage);

        // Act
        PageResult<StockResponse> result = getLowStockService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();

        // Verify interactions
        verify(stockRepository).findLowStockPaged(pageQuery);
    }
}