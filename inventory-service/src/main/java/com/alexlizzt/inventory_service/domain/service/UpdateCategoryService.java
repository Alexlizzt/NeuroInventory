package com.alexlizzt.inventory_service.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.command.UpdateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.mapper.CategoryDtoMapper;
import com.alexlizzt.inventory_service.application.usecase.UpdateCategoryUseCase;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCategoryService implements UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final CategoryDtoMapper categoryDtoMapper;

    @Transactional
    public CategoryResponse execute(UpdateCategoryCommand command) {

        Category category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new CategoryNotFoundException(command.id()));

        category.updateDetails(
                command.name(),
                command.description()
        );

        Category updatedCategory = categoryRepository.save(category);

        return categoryDtoMapper.toResponse(updatedCategory);
    }
}
