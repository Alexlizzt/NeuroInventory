package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;


@Getter
@AllArgsConstructor
@ToString
public class Category {
    private String id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public void update(String name, String description, LocalDateTime updatedAt) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        this.name = name;
        this.description = description;
        this.updatedAt = updatedAt;
    }
}
