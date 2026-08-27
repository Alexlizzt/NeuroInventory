package com.alexlizzt.inventory_service.infraestructure.persistence.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.StockEntity;

@Mapper(componentModel = "spring")
public interface StockEntityMapper {
    Stock toDomain(StockEntity entity);

    @InheritInverseConfiguration
    StockEntity toEntity(Stock domain);
}
