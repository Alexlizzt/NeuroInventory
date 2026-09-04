package com.alexlizzt.inventory_service.domain.exception;

public class DuplicateProductNameException extends RuntimeException {

    public DuplicateProductNameException(String name) {
        super("A product with name '" + name + "' already exists.");
    }

}
