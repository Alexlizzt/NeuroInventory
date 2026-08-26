package com.alexlizzt.inventory_service.domain.repository;

import java.util.Optional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(String id);
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
    PageResult<Category> findAllPaged(PageQuery pageQuery);
    void deleteById(String id);
}
