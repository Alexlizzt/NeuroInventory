package com.alexlizzt.inventory_service.infraestructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.infraestructure.persistence.mapper.CategoryEntityMapper;
import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.CategoryJpaRepository;

public class CategoryRepositoryAdapter implements CategoryRepository {
    
    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryEntityMapper mapper;

    public CategoryRepositoryAdapter(CategoryJpaRepository categoryJpaRepository, CategoryEntityMapper mapper) {
        this.categoryJpaRepository = categoryJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Category> findById(String id) {
        return categoryJpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Category> findByName(String name) {
        return categoryJpaRepository.findByName(name)
                .map(mapper::toDomain);
    }


    public List<Category> findAll() {
        return categoryJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Category save(Category category) {
        var entity = mapper.toEntity(category);
        var savedEntity = categoryJpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    public boolean existsByName(String name) {
        return categoryJpaRepository.existsByName(name);
    }

    public void deleteById(String id) {
        categoryJpaRepository.deleteById(id);
    }

    @Override
    public PageResult<Category> findAllPaged(PageQuery pageQuery) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findAllPaged'");
    }

}
