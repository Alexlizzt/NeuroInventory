package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryMovementDtoMapper;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class GetInventoryHistoryUseCase {
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementDtoMapper movementDtoMapper;

    public GetInventoryHistoryUseCase(
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository,
            InventoryMovementDtoMapper movementDtoMapper) {
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.movementDtoMapper = movementDtoMapper;
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> execute(String productId) {
        if (!productRepository.findById(productId).isPresent()) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }

        return movementRepository.findByProductId(productId).stream()
                .map(movementDtoMapper::toResponse)
                .toList();
    }
}
