package com.alexlizzt.inventory_service.application.command;

public record UpdateCategoryCommand(
    String id,
    String name,
    String description
) { 
    public UpdateCategoryCommand {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Category ID cannot be null or empty");
        }
    }
}
