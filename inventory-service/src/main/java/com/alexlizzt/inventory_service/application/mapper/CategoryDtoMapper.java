package com.alexlizzt.inventory_service.application.mapper;

import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.domain.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryDtoMapper {
    CategoryResponse toResponse(Category category);
}
