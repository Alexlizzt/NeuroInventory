package com.alexlizzt.inventory_service.domain.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.command.CreateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.application.usecase.CreateCategoryUseCase;
import com.alexlizzt.inventory_service.domain.exception.DuplicateCategoryNameException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;


@Service
public class CreateCategoryService implements CreateCategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    public CreateCategoryService(CategoryRepository categoryRepository, CategoryDtoMapper categoryDtoMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryDtoMapper = categoryDtoMapper;
    }

    @Override
    @Transactional
    public CategoryResponse execute(CreateCategoryCommand command) {
        if (categoryRepository.existsByName(command.name())) {
            throw new DuplicateCategoryNameException(command.name());
        }

        Category category = Category.builder()
                .id(UUID.randomUUID().toString())
                .name(command.name())
                .description(command.description())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return categoryDtoMapper.toResponse(savedCategory);
    }
}
