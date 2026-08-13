package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Stock {
    private String productId;
    private int quantity;
    private int minStock;
    private LocalDateTime updatedAt;

    public void update(int quantity, int minStock, LocalDateTime updateTime) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La cantidad en stock no puede ser negativa");
        }
        this.quantity = quantity;
        this.minStock = minStock;
        this.updatedAt = updateTime;
    }
}
