package com.alexlizzt.inventory_service.application.usecase.query;

public record PageQuery(
    int page,
        int size,
        String sortBy,
        String direction
) { }
