package com.alexlizzt.inventory_service.application.usecase.query;

import java.util.List;

public record PageResult<T>(
    List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) { }
