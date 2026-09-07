package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.command.UpdateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
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
class UpdateProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductDtoMapper productDtoMapper;

    @InjectMocks
    private UpdateProductService updateProductService;

    @Test
    @DisplayName("Debe actualizar el producto exitosamente cuando existe y no cambia de categoría o es válida")
    void shouldUpdateProductSuccessfully() {
        // Arrange
        String productId = "prod-123";
        String categoryId = "cat-1";
        UpdateProductCommand command = new UpdateProductCommand(
                productId, "New Name", "New Desc", new BigDecimal("150.00"), categoryId, true
        );

        Product existingProduct = Product.builder()
                .id(productId)
                .categoryId(categoryId)
                .name("Old Name")
                .price(new BigDecimal("100.00"))
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse expectedResponse = new ProductResponse(
                productId, "SKU-1", "New Name", "New Desc",
                new BigDecimal("150.00"),true, categoryId, null, null
        );
        when(productDtoMapper.toResponse(existingProduct)).thenReturn(expectedResponse);

        // Act
        ProductResponse response = updateProductService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.name()).isEqualTo("New Name");

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(categoryRepository, never()).findById(any()); // No cambia de categoría, no debería buscarla
        verify(productRepository).save(existingProduct);
        verify(productDtoMapper).toResponse(existingProduct);
    }

    @Test
    @DisplayName("Debe validar la nueva categoría y actualizar exitosamente si la categoría existe y es diferente")
    void shouldUpdateProductAndValidateNewCategorySuccessfully() {
        // Arrange
        String productId = "prod-123";
        String oldCategoryId = "cat-1";
        String newCategoryId = "cat-2";
        UpdateProductCommand command = new UpdateProductCommand(
                productId, "New Name", "New Desc", new BigDecimal("150.00"), newCategoryId, true
        );

        Product existingProduct = Product.builder()
                .id(productId)
                .categoryId(oldCategoryId)
                .name("Old Name")
                .price(new BigDecimal("100.00"))
                .active(true)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(newCategoryId)).thenReturn(Optional.of(Category.builder().id(newCategoryId).build()));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        ProductResponse expectedResponse = new ProductResponse(
                productId, "SKU-1", "New Name", "New Desc",
                new BigDecimal("150.00"), true, newCategoryId, null, null
        );
        when(productDtoMapper.toResponse(existingProduct)).thenReturn(expectedResponse);

        // Act
        ProductResponse response = updateProductService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.categoryId()).isEqualTo(newCategoryId);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(categoryRepository).findById(newCategoryId);
        verify(productRepository).save(existingProduct);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si el producto a actualizar no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        String productId = "prod-999";
        UpdateProductCommand command = new UpdateProductCommand(
                productId, "Name", "Desc", new BigDecimal("100.00"), "cat-1", true
        );

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateProductService.execute(command))
                .isInstanceOf(ProductNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(categoryRepository, never()).findById(any());
        verify(productRepository, never()).save(any());
        verify(productDtoMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Debe lanzar CategoryNotFoundException si la nueva categoría no existe")
    void shouldThrowExceptionWhenNewCategoryNotFound() {
        // Arrange
        String productId = "prod-123";
        String oldCategoryId = "cat-1";
        String nonExistentCategoryId = "cat-999";
        UpdateProductCommand command = new UpdateProductCommand(
                productId, "Name", "Desc", new BigDecimal("100.00"), nonExistentCategoryId, true
        );

        Product existingProduct = Product.builder()
                .id(productId)
                .categoryId(oldCategoryId)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(categoryRepository.findById(nonExistentCategoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateProductService.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(categoryRepository).findById(nonExistentCategoryId);
        verify(productRepository, never()).save(any());
        verify(productDtoMapper, never()).toResponse(any());
    }
}