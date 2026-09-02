package com.alexlizzt.inventory_service.domain.exception;

public class InvalidInventoryMovementTypeException extends RuntimeException {

    public InvalidInventoryMovementTypeException(String type) {
        super("Invalid inventory movement type: " + type + ".");
    }
}