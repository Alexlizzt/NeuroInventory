package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class Product {
    private String id;
    private String categoryId;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void update(String categoryId, String name, String description, BigDecimal price, Boolean active, LocalDateTime updatedAt) {
        if (price != null && price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo");
        }
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (categoryId != null) {
            this.categoryId = categoryId;
        }
        this.description = description;
        if (price != null) {
            this.price = price;
        }
        if (active != null) {
            this.active = active;
        }
        this.updatedAt = updatedAt;
    }
}
