package com.alexlizzt.inventory_service.application.usecase.command;

public record UpdateCategoryCommand(
    String id,
    String name,
    String description
) { }
