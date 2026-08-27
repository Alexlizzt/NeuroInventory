package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
    Optional<ProductEntity> findBySku(String sku);
    boolean existsBySku(String sku);
    List<ProductEntity> findByNameContainingIgnoreCaseAndCategoryId(String name, String categoryId);
}
