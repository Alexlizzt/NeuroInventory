package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListCategoriesUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public PageResult<Category> execute(PageQuery pageQuery) {
        // Sanitizar parámetros de paginación básicos
        int page = pageQuery.page() < 0 ? 0 : pageQuery.page();
        int size = pageQuery.size() <= 0 ? 10 : pageQuery.size();

        PageQuery sanitizedQuery = new PageQuery(page, size, pageQuery.sortBy(), pageQuery.direction());

        return categoryRepository.findAllPaged(sanitizedQuery);
    }
}
