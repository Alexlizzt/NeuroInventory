package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;


@Service
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    public ListCategoriesUseCase(CategoryRepository categoryRepository, CategoryDtoMapper categoryDtoMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryDtoMapper = categoryDtoMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<CategoryResponse> execute(PageQuery pageQuery) {
        PageResult<Category> categoryPage = categoryRepository.findAllPaged(pageQuery);

        return new PageResult<>(
                categoryPage.content().stream()
                        .map(categoryDtoMapper::toResponse)
                        .toList(),
                categoryPage.pageNumber(),
                categoryPage.pageSize(),
                categoryPage.totalElements(),
                categoryPage.totalPages()
        );
    }
}
