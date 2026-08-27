package com.alexlizzt.inventory_service.application.command;

public record UpdateCategoryCommand(
    String id,
    String name,
    String description
) { }
