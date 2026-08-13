package com.alexlizzt.inventory_service.domain.repository;

import java.util.Optional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Product;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    Optional<Product> findBySku(String sku);
    PageResult<Product> findAllPaged(PageQuery pageQuery);
    void delete(String id);
}
