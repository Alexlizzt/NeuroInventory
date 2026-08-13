package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class InventoryMovement {

    private String id;
    private String productId;
    private MovementType type;
    private int quantity;
    private String reason;
    private String userId;
    private LocalDateTime createdAt;

    public static InventoryMovement create(
            String id, 
            String productId, 
            MovementType type, 
            int quantity, 
            String reason, 
            String userId, 
            LocalDateTime createdAt) {
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor a cero");
        }
        
        if (type == null) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        return new InventoryMovement(id, productId, type, quantity, reason, userId, createdAt);
    }
}