package com.alexlizzt.inventory_service.domain.repository;

import java.util.List;
import java.util.Optional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Product;


public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    List<Product> searchByNameAndCategory(String name, String categoryId);
    List<Product> findAllByIds(List<String> ids);
    PageResult<Product> findAllPaged(PageQuery pageQuery);
    void deleteById(String id);
}
