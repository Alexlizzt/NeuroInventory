package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.command.DeleteProductCommand;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;

    @Transactional
    public void execute(DeleteProductCommand command) {
        // 1. Buscar si el producto existe en el dominio
        productRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + command.id()));

        // 2. Validar la restricción de la Base de Datos (ON DELETE RESTRICT)
        // Si tiene movimientos históricos, impedimos el borrado físico a nivel de aplicación.
        boolean hasMovements = !inventoryMovementRepository.findByProductId(command.id()).isEmpty();
        if (hasMovements) {
            throw new IllegalStateException("No se puede eliminar el producto porque tiene movimientos de inventario registrados.");
        }

        // 3. Si pasa la validación, procedemos a borrar.
        // Nota: Gracias al CASCADE en tu SQL, si el producto se borra, 
        // la base de datos limpiará automáticamente su registro en 'stocks' y 'product_embeddings'.
        productRepository.delete(command.id());
    }
}
