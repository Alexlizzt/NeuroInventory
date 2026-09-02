package com.alexlizzt.inventory_service.domain.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String id) {
        super("No category exists with identifier " + id + ".");
    }
}