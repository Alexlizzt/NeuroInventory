package com.alexlizzt.inventory_service.application.usecase.command;

import com.alexlizzt.inventory_service.domain.model.MovementType;

public record RegisterInventoryMovementCommand (
    String productId,
    MovementType type,
    int quantity,
    String reason,
    String userId
) { }
