package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {
    private String productId;
    private Integer quantity;
    private Integer minStock;
    private LocalDateTime updatedAt;

    /**
     * Regla de negocio: Determina si el producto está en nivel crítico de inventario.
     */
    public boolean isLowStock() {
        if (this.quantity == null || this.minStock == null) {
            return false;
        }
        return this.quantity <= this.minStock;
    }

    /**
     * Regla de negocio: Incrementa existencias y actualiza la fecha de modificación.
     */
    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be greater than zero.");
        }
        this.quantity = (this.quantity == null ? 0 : this.quantity) + amount;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Regla de negocio: Disminuye existencias y actualiza la fecha de modificación.
     */
    public void removeQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to remove must be greater than zero.");
        }
        int current = (this.quantity == null ? 0 : this.quantity);
        if (current < amount) {
            throw new IllegalStateException("Insufficient stock. Current: " + current + ", Requested: " + amount);
        }
        this.quantity = current - amount;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
