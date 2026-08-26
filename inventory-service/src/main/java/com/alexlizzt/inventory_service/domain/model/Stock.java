package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Stock {
    private String productId;
    private Integer quantity;
    private Integer minStock;
    private LocalDateTime updatedAt;

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be positive");
        }
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public void subtractQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to subtract must be positive");
        }
        if (this.quantity < amount) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        this.quantity -= amount;
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
