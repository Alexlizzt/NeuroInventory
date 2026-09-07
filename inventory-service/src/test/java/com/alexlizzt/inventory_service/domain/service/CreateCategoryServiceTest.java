package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.command.CreateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.DuplicateCategoryNameException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateCategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryDtoMapper categoryDtoMapper;

    @InjectMocks
    private CreateCategoryService createCategoryService;

    @Test
    @DisplayName("Debe crear la categoría exitosamente cuando el nombre no existe previamente")
    void shouldCreateCategorySuccessfully() {
        // Arrange
        CreateCategoryCommand command = new CreateCategoryCommand("Electronics", "Electronic devices");

        when(categoryRepository.existsByName("Electronics")).thenReturn(false);

        Category savedCategory = Category.builder()
                .id("uuid-123")
                .name("Electronics")
                .description("Electronic devices")
                .build();
        
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponse expectedResponse = new CategoryResponse("uuid-123", "Electronics", "Electronic devices");
        when(categoryDtoMapper.toResponse(savedCategory)).thenReturn(expectedResponse);

        // Act
        CategoryResponse response = createCategoryService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("uuid-123");
        assertThat(response.name()).isEqualTo("Electronics");

        // Verify interactions
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository).save(any(Category.class));
        verify(categoryDtoMapper).toResponse(savedCategory);
    }

    @Test
    @DisplayName("Debe lanzar DuplicateCategoryNameException si el nombre de la categoría ya existe")
    void shouldThrowExceptionWhenCategoryNameExists() {
        // Arrange
        CreateCategoryCommand command = new CreateCategoryCommand("Electronics", "Electronic devices");

        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> createCategoryService.execute(command))
                .isInstanceOf(DuplicateCategoryNameException.class);

        // Verify interactions - ensure save and mapper are never called
        verify(categoryRepository).existsByName("Electronics");
        verify(categoryRepository, never()).save(any());
        verify(categoryDtoMapper, never()).toResponse(any());
    }
}
