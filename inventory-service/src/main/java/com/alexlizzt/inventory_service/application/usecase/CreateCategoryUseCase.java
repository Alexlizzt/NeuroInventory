package com.alexlizzt.inventory_service.application.usecase;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateCategoryUseCase {

    private final CategoryRepository categoryRepository;
    private final Clock clock;

    @Transactional
    public Category execute(String name, String description) {
        // 1. Validar si ya existe una categoría con el mismo nombre (según la restricción UNIQUE de la BD)
        categoryRepository.findByName(name).ifPresent(category -> {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + name);
        });

        // 2. Obtener la fecha y hora segura con el clock
        LocalDateTime now = LocalDateTime.now(clock);
        String categoryId = UUID.randomUUID().toString();

        // 3. Crear el objeto de dominio Category
        Category category = new Category(
                categoryId,
                name,
                description,
                now,
                now
        );

        // 4. Guardar y retornar
        return categoryRepository.save(category);
    }
}
