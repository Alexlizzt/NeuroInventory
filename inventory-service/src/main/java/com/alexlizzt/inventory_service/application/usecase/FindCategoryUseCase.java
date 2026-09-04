package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;

@FunctionalInterface 
public interface FindCategoryUseCase {
    
    public CategoryResponse execute(String id);
}
