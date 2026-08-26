package com.alexlizzt.inventory_service.domain.repository;

import java.util.Optional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Stock;

public interface StockRepository {
    Stock save(Stock stock);
    Optional<Stock> findByProductId(String productId);
    PageResult<Stock> findLowStockPaged(PageQuery pageQuery);
    PageResult<Stock> findAllPaged(PageQuery pageQuery);
}
