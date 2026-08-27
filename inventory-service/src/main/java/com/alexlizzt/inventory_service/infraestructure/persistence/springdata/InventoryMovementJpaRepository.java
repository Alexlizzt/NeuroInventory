package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.InventoryMovementEntity;

public interface InventoryMovementJpaRepository extends JpaRepository<InventoryMovementEntity, String> {
    List<InventoryMovementEntity> findByProductId(String productId);
}
