package com.alexlizzt.inventory_service.domain.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("No product exists with identifier " + id + ".");
    }
}