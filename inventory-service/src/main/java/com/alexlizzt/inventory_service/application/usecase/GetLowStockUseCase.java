package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;


@FunctionalInterface 
public interface GetLowStockUseCase {
    
    PageResult<StockResponse> execute(PageQuery pageQuery);
}
