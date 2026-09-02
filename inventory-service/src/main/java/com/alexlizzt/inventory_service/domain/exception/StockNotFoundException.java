package com.alexlizzt.inventory_service.domain.exception;

public class StockNotFoundException extends RuntimeException {

    public StockNotFoundException(String productId) {
        super("No stock exists for product with identifier " + productId + ".");
    }
}