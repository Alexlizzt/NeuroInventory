package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.StockEntity;

public interface StockJpaRepository extends JpaRepository<StockEntity, String> {
    @Query("SELECT s FROM StockEntity s WHERE s.quantity <= s.minStock")
    Page<StockEntity> findLowStock(Pageable pageable);
}
