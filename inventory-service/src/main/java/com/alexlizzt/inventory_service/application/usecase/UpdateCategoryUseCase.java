package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.command.UpdateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;

@FunctionalInterface 
public interface UpdateCategoryUseCase {

    CategoryResponse execute(UpdateCategoryCommand command);

}
