package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
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
class GetCategoriesServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryDtoMapper categoryDtoMapper;

    @InjectMocks
    private GetCategoriesService getCategoriesService;

    @Test
    @DisplayName("Debe retornar una página paginada de categorías mapeadas exitosamente")
    void shouldReturnPagedCategoriesSuccessfully() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        Category category = Category.builder()
                .id("cat-1")
                .name("Electronics")
                .description("Devices")
                .build();

        PageResult<Category> categoryPage = new PageResult<>(
                List.of(category),
                0,
                10,
                1L,
                1
        );

        when(categoryRepository.findAllPaged(pageQuery)).thenReturn(categoryPage);

        CategoryResponse categoryResponse = new CategoryResponse(
                "cat-1", "Electronics", "Devices"
        );
        when(categoryDtoMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        PageResult<CategoryResponse> result = getCategoriesService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo("cat-1");
        assertThat(result.content().get(0).name()).isEqualTo("Electronics");
        assertThat(result.pageNumber()).isZero();
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);

        // Verify interactions
        verify(categoryRepository).findAllPaged(pageQuery);
        verify(categoryDtoMapper).toResponse(category);
    }

    @Test
    @DisplayName("Debe retornar una página vacía si no existen categorías")
    void shouldReturnEmptyPageWhenNoCategoriesExist() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        PageResult<Category> emptyPage = new PageResult<>(
                List.of(),
                0,
                10,
                0L,
                0
        );

        when(categoryRepository.findAllPaged(pageQuery)).thenReturn(emptyPage);

        // Act
        PageResult<CategoryResponse> result = getCategoriesService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();

        // Verify interactions
        verify(categoryRepository).findAllPaged(pageQuery);
    }
}