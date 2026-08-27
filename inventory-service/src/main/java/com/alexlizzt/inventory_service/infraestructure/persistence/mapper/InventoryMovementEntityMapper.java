package com.alexlizzt.inventory_service.infraestructure.persistence.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.InventoryMovementEntity;

@Mapper(componentModel = "spring")
public interface InventoryMovementEntityMapper {
    
    InventoryMovement toDomain(InventoryMovementEntity entity);

    @InheritInverseConfiguration
    InventoryMovementEntity toEntity(InventoryMovement domain);
}
