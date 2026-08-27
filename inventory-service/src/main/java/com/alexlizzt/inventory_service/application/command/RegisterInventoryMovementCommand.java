package com.alexlizzt.inventory_service.application.command;

public record RegisterInventoryMovementCommand (
    String productId,
    String type, // "IN", "OUT", "ADJUSTMENT"
    int quantity,
    String reason,
    String userId
) { }
