package com.alexlizzt.inventory_service.domain.service;

import com.alexlizzt.inventory_service.application.command.CreateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.DuplicateSkuException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductDtoMapper productDtoMapper;

    @InjectMocks
    private CreateProductService createProductService;

    @Test
    @DisplayName("Debe crear el producto y su stock inicial exitosamente")
    void shouldCreateProductAndInitialStockSuccessfully() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                "cat-123",
                "SKU-PROD-1",
                "Laptop",
                "High performance laptop",
                new BigDecimal("1200.00"),
                10,
                2
        );

        when(categoryRepository.findById("cat-123")).thenReturn(Optional.of(new Category()));
        when(productRepository.existsBySku("SKU-PROD-1")).thenReturn(false);

        Product savedProduct = Product.builder()
                .id("uuid-prod-1")
                .categoryId("cat-123")
                .sku("SKU-PROD-1")
                .name("Laptop")
                .description("High performance laptop")
                .price(new BigDecimal("1200.00"))
                .active(true)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);
        when(stockRepository.save(any(Stock.class))).thenReturn(new Stock());

        ProductResponse expectedResponse = new ProductResponse(
			"uuid-prod-1",
			"SKU-PROD-1",
			"Laptop",
			"High performance laptop",
			new BigDecimal("1200.00"),
			true,
			"cat-123",
			null,
			null
			);

        when(productDtoMapper.toResponse(savedProduct)).thenReturn(expectedResponse);

        // Act
        ProductResponse response = createProductService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("uuid-prod-1");
        assertThat(response.sku()).isEqualTo("SKU-PROD-1");
        assertThat(response.name()).isEqualTo("Laptop");

        // Verify interactions
        verify(categoryRepository).findById("cat-123");
        verify(productRepository).existsBySku("SKU-PROD-1");
        verify(productRepository).save(any(Product.class));
        verify(stockRepository).save(any(Stock.class));
        verify(productDtoMapper).toResponse(savedProduct);
    }

    @Test
    @DisplayName("Debe lanzar CategoryNotFoundException si la categoría no existe")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                "cat-999", "SKU-PROD-1", "Laptop", "Desc", new BigDecimal("100.00"), 5, 1
        );

        when(categoryRepository.findById("cat-999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> createProductService.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);

        // Verify interactions
        verify(categoryRepository).findById("cat-999");
        verify(productRepository, never()).existsBySku(any());
        verify(productRepository, never()).save(any());
        verify(stockRepository, never()).save(any());
        verify(productDtoMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Debe lanzar DuplicateSkuException si el SKU ya existe")
    void shouldThrowExceptionWhenSkuExists() {
        // Arrange
        CreateProductCommand command = new CreateProductCommand(
                "cat-123", "SKU-EXISTING", "Laptop", "Desc", new BigDecimal("100.00"), 5, 1
        );

        when(categoryRepository.findById("cat-123")).thenReturn(Optional.of(new Category()));
        when(productRepository.existsBySku("SKU-EXISTING")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> createProductService.execute(command))
                .isInstanceOf(DuplicateSkuException.class);

        // Verify interactions
        verify(categoryRepository).findById("cat-123");
        verify(productRepository).existsBySku("SKU-EXISTING");
        verify(productRepository, never()).save(any());
        verify(stockRepository, never()).save(any());
        verify(productDtoMapper, never()).toResponse(any());
    }
}