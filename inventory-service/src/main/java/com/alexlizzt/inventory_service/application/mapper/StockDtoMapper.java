package com.alexlizzt.inventory_service.application.mapper;

import org.mapstruct.Mapper;

import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.domain.model.Stock;

@Mapper(componentModel = "spring")
public interface StockDtoMapper {
    StockResponse toResponse(Stock stock);
}
