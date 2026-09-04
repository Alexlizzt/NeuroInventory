package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.command.CreateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;

public interface CreateCategoryUseCase {

    CategoryResponse execute(CreateCategoryCommand command);
    
}
