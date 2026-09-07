package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FindCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryDtoMapper categoryDtoMapper;

    @InjectMocks
    private FindCategoryService findCategoryService;

    @Test
    @DisplayName("Debe retornar la categoría mapeada exitosamente cuando el ID existe")
    void shouldFindCategorySuccessfullyWhenIdExists() {
        // Arrange
        String categoryId = "cat-123";
        Category category = Category.builder()
                .id(categoryId)
                .name("Electronics")
                .description("Electronic devices")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId, "Electronics", "Electronic devices"
        );
        when(categoryDtoMapper.toResponse(category)).thenReturn(expectedResponse);

        // Act
        CategoryResponse response = findCategoryService.execute(categoryId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Electronics");

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryDtoMapper).toResponse(category);
    }

    @Test
    @DisplayName("Debe lanzar CategoryNotFoundException si la categoría no existe")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        String categoryId = "cat-999";

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> findCategoryService.execute(categoryId))
                .isInstanceOf(CategoryNotFoundException.class);

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryDtoMapper, never()).toResponse(any());
    }
}