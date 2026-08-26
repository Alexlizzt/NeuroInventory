package com.alexlizzt.inventory_service.infraestructure.persistence.entity;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.infraestructure.persistence.mapper.ProductEntityMapper;

@Mapper(componentModel = "spring", uses = {ProductEntityMapper.class})
public interface InventoryMovementEntityMapper {
    @Mapping(target = "product", source = "product")
    InventoryMovement toDomain(InventoryMovementEntity entity);

    @InheritInverseConfiguration
    InventoryMovementEntity toEntity(InventoryMovement domain);
}
