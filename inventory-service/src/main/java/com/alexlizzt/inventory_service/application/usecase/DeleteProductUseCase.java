package com.alexlizzt.inventory_service.application.usecase;

@FunctionalInterface 
public interface DeleteProductUseCase {

    void execute(String id);
}
