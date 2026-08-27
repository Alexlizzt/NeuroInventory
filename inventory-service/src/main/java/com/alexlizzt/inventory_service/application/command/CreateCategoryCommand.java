package com.alexlizzt.inventory_service.application.command;

public record CreateCategoryCommand(
    String name,
    String description
) {
    
}
