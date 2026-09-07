package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;

@FunctionalInterface 
public interface ListProductsUseCase {
    
    PageResult<ProductResponse> execute(PageQuery pageQuery);
}
