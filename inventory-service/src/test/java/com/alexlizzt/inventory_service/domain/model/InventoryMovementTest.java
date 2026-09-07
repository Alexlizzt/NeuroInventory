package com.alexlizzt.inventory_service.domain.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InventoryMovementTest {

    @Test
    @DisplayName("Debe crear la instancia correctamente usando el Builder y recuperar los valores con los Getters")
    void shouldCreateInventoryMovementUsingBuilder() {
        // Arrange
        String id = "mov-123";
        String productId = "prod-456";
        String type = "IN";
        int quantity = 50;
        String reason = "Purchase order";
        String userId = "user-789";
        LocalDateTime now = LocalDateTime.now();

        // Act
        InventoryMovement movement = InventoryMovement.builder()
                .id(id)
                .productId(productId)
                .type(type)
                .quantity(quantity)
                .reason(reason)
                .userId(userId)
                .createdAt(now)
                .build();

        // Assert
        assertThat(movement).isNotNull();
        assertThat(movement.getId()).isEqualTo(id);
        assertThat(movement.getProductId()).isEqualTo(productId);
        assertThat(movement.getType()).isEqualTo(type);
        assertThat(movement.getQuantity()).isEqualTo(quantity);
        assertThat(movement.getReason()).isEqualTo(reason);
        assertThat(movement.getUserId()).isEqualTo(userId);
        assertThat(movement.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Debe funcionar correctamente con el constructor de todos los argumentos")
    void shouldCreateUsingAllArgsConstructor() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        InventoryMovement movement = new InventoryMovement(
                "1", "prod-1", "OUT", 5, "Sale", "user-1", now
        );

        // Assert
        assertThat(movement).isNotNull();
        assertThat(movement.getId()).isEqualTo("1");
        assertThat(movement.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("Debe funcionar correctamente con el constructor vacío")
    void shouldCreateUsingNoArgsConstructor() {
        // Act
        InventoryMovement movement = new InventoryMovement();

        // Assert
        assertThat(movement).isNotNull();
        assertThat(movement.getId()).isNull();
        assertThat(movement.getQuantity()).isZero();
    }
}