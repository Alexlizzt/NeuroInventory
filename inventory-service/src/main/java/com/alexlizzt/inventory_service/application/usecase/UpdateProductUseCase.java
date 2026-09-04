package com.alexlizzt.inventory_service.application.usecase;
import com.alexlizzt.inventory_service.application.command.UpdateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;

@FunctionalInterface
public interface UpdateProductUseCase {
    ProductResponse execute(UpdateProductCommand command);
}