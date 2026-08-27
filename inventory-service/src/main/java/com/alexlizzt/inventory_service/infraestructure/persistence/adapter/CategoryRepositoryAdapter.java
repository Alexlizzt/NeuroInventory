package com.alexlizzt.inventory_service.infraestructure.persistence.adapter;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;
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
    Sort sort = pageQuery.direction().equalsIgnoreCase("DESC") 
            ? Sort.by(pageQuery.sortBy()).descending() 
            : Sort.by(pageQuery.sortBy()).ascending();

    PageRequest pageable = PageRequest.of(pageQuery.page(), pageQuery.size(), sort);

    Page<CategoryEntity> entityPage = categoryJpaRepository.findAll(pageable);

    List<Category> domainList = entityPage.getContent().stream()
            .map(mapper::toDomain)
            .toList();

    return new PageResult<>(
            domainList,
            entityPage.getNumber(),
            entityPage.getSize(),
            entityPage.getTotalElements(),
            entityPage.getTotalPages()
    );
}

}
