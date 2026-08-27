package com.alexlizzt.inventory_service.application.usecase;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.command.UpdateCategoryCommand;
import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Category execute(UpdateCategoryCommand command) {
        // 1. Buscar si la categoría existe
        Category category = categoryRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + command.id()));


        // 3. Actualizar datos aplicando el comportamiento del dominio
        category.updateDetails(command.name(), command.description());

        // 4. Persistir y retornar el resultado actualizado
        return categoryRepository.save(category);
    }
}
