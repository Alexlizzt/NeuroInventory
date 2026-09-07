package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.command.CreateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;

@FunctionalInterface 
public interface CreateProductUseCase {
    
    ProductResponse execute(CreateProductCommand command);
}
