package com.alexlizzt.inventory_service.application.usecase;

import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;

@FunctionalInterface 
public interface GetCategoriesUseCase {

    public PageResult<CategoryResponse> execute(PageQuery pageQuery);

}
