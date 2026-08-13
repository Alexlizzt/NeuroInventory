package com.alexlizzt.inventory_service.application.usecase;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.command.UpdateProductCommand;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final Clock clock;
    private final ProductRepository productRepository;

    @Transactional
    public Product execute(UpdateProductCommand command) {
        // 1. Buscar si el producto existe en el dominio
        Product existingProduct = productRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + command.id()));

        // 2. Obtener la hora segura con el clock
        LocalDateTime now = LocalDateTime.now(clock);
        
        // 3. Actualizar los campos del producto (puedes tener un método en la entidad Product o hacerlo aquí)
        existingProduct.update(
                command.categoryId(),
                command.name(),
                command.description(),
                command.price(),
                command.active(),
                now
        );

        // 3. Persistir los cambios
        return productRepository.save(existingProduct);
    }                
}
