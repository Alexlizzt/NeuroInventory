package com.alexlizzt.inventory_service.infraestructure.persistence.mapper;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.domain.model.Category;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryEntityMapper {
    Category toDomain(CategoryEntity entity);

    @InheritInverseConfiguration
    CategoryEntity toEntity(Category domain);
}
