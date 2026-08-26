package com.alexlizzt.inventory_service.domain.repository;

import java.util.List;
import java.util.Optional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;

public interface InventoryMovementRepository {
    InventoryMovement save(InventoryMovement movement);
    Optional<InventoryMovement> findById(String id);
    List<InventoryMovement> findByProductId(String productId);
    PageResult<InventoryMovement> findAllPaged(PageQuery pageQuery);
}
