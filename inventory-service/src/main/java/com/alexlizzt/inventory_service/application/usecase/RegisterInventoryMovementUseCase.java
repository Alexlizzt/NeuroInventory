package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.command.RegisterInventoryMovementCommand;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;

@FunctionalInterface 
public interface RegisterInventoryMovementUseCase {

    InventoryMovementResponse execute(RegisterInventoryMovementCommand command);
}
