package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
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
class FindProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDtoMapper productDtoMapper;

    @InjectMocks
    private FindProductService findProductService;

    @Test
    @DisplayName("Debe retornar el producto mapeado exitosamente cuando el ID existe")
    void shouldFindProductSuccessfullyWhenIdExists() {
        // Arrange
        String productId = "prod-123";
        Product product = Product.builder()
                .id(productId)
                .sku("SKU-123")
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse expectedResponse = new ProductResponse(
                productId, "SKU-123", "Laptop", "Gaming laptop",
                new BigDecimal("1500.00"), true,"cat-1", null, null
        );
        when(productDtoMapper.toResponse(product)).thenReturn(expectedResponse);

        // Act
        ProductResponse response = findProductService.execute(productId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.sku()).isEqualTo("SKU-123");
        assertThat(response.name()).isEqualTo("Laptop");

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(productDtoMapper).toResponse(product);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si el producto no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        String productId = "prod-999";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> findProductService.execute(productId))
                .isInstanceOf(ProductNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(productDtoMapper, never()).toResponse(any());
    }
}