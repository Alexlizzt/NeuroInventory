package com.alexlizzt.inventory_service.domain.repository;

import java.util.List;
import java.util.Optional;

import com.alexlizzt.inventory_service.domain.model.InventoryMovement;

public interface InventoryMovementRepository {
    // Guardar un nuevo movimiento en el historial
    InventoryMovement save(InventoryMovement movement);

    // Buscar un movimiento específico por su ID
    Optional<InventoryMovement> findById(String id);

    // Consultar el historial de movimientos de un producto específico
    List<InventoryMovement> findByProductId(String productId);
}
