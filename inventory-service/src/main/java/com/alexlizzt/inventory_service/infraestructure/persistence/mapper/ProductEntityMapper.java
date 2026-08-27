package com.alexlizzt.inventory_service.infraestructure.persistence.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring", uses = {CategoryEntityMapper.class})
public interface ProductEntityMapper {
    Product toDomain(ProductEntity entity);

    @InheritInverseConfiguration
    ProductEntity toEntity(Product domain);
}
