package com.alexlizzt.inventory_service.domain.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productId) {
        super("Insufficient stock for product with identifier " + productId + ".");
    }
}