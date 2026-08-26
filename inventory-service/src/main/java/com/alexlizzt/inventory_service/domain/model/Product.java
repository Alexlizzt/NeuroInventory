package com.alexlizzt.inventory_service.domain.model;

import java.math.BigDecimal;
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
public class Product {
    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String categoryId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Método de dominio para mutar el estado de forma controlada
    public void updateDetails(String name, String description, BigDecimal price, String categoryId, Boolean active) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) >= 0) {
            this.price = price;
        }
        if (categoryId != null && !categoryId.isBlank()) {
            this.categoryId = categoryId;
        }
        if (active != null) {
            this.active = active;
        }
        this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
