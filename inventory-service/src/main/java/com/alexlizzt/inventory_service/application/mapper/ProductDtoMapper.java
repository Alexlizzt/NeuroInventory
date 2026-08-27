package com.alexlizzt.inventory_service.application.mapper;

import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.domain.model.Product;


@Mapper(componentModel = "spring")
public interface ProductDtoMapper {
    ProductResponse toResponse(Product product);
}
