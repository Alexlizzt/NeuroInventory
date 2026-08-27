package com.alexlizzt.inventory_service.application.query;

public record PageQuery(
    int page,
        int size,
        String sortBy,
        String direction
) { 
    // Constructor secundario con valores por defecto
    public PageQuery(int page, int size) {
        this(page, size, "id", "ASC");
    }
}
