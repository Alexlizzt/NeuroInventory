package com.alexlizzt.inventory_service.application.mapper;

import org.mapstruct.Mapper;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;


@Mapper(componentModel = "spring")
public interface InventoryMovementDtoMapper {
    InventoryMovementResponse toResponse(InventoryMovement movement);

}
