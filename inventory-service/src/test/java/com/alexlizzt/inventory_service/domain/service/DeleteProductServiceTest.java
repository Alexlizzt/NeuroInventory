package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private DeleteProductService deleteProductService;

    @Test
    @DisplayName("Debe eliminar el producto exitosamente cuando existe")
    void shouldDeleteProductSuccessfullyWhenExists() {
        // Arrange
        String productId = "prod-123";
        Product existingProduct = Product.builder().id(productId).build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        doNothing().when(productRepository).deleteById(productId);

        // Act & Assert
        assertThatCode(() -> deleteProductService.execute(productId))
                .doesNotThrowAnyException();

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(productRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si el producto no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        String productId = "prod-999";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> deleteProductService.execute(productId))
                .isInstanceOf(ProductNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(productRepository, never()).deleteById(anyString());
    }
}