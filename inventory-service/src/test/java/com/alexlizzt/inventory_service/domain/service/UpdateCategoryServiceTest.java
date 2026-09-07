package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.command.UpdateCategoryCommand;
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
class UpdateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryDtoMapper categoryDtoMapper;

    @InjectMocks
    private UpdateCategoryService updateCategoryService;

    @Test
    @DisplayName("Debe actualizar los detalles de la categoría exitosamente cuando el ID existe")
    void shouldUpdateCategorySuccessfullyWhenIdExists() {
        // Arrange
        String categoryId = "cat-123";
        UpdateCategoryCommand command = new UpdateCategoryCommand(categoryId, "Updated Name", "Updated Description");

        Category existingCategory = Category.builder()
                .id(categoryId)
                .name("Old Name")
                .description("Old Description")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId, "Updated Name", "Updated Description"
        );
        when(categoryDtoMapper.toResponse(existingCategory)).thenReturn(expectedResponse);

        // Act
        CategoryResponse response = updateCategoryService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo("Updated Name");
        assertThat(response.description()).isEqualTo("Updated Description");

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).save(existingCategory);
        verify(categoryDtoMapper).toResponse(existingCategory);
    }

    @Test
    @DisplayName("Debe lanzar CategoryNotFoundException si la categoría a actualizar no existe")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        String categoryId = "cat-999";
        UpdateCategoryCommand command = new UpdateCategoryCommand(categoryId, "Name", "Description");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> updateCategoryService.execute(command))
                .isInstanceOf(CategoryNotFoundException.class);

        // Verify interactions
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).save(any());
        verify(categoryDtoMapper, never()).toResponse(any());
    }
}