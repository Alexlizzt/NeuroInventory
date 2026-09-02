package com.alexlizzt.inventory_service.application.usecase;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.command.RegisterInventoryMovementCommand;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryMovementDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.InvalidInventoryMovementTypeException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.StockNotFoundException;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;


@Service
public class RegisterInventoryMovementUseCase {

    private final InventoryMovementRepository movementRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final InventoryMovementDtoMapper movementDtoMapper;

    public RegisterInventoryMovementUseCase(
            InventoryMovementRepository movementRepository,
            StockRepository stockRepository,
            ProductRepository productRepository,
            InventoryMovementDtoMapper movementDtoMapper) {
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.movementDtoMapper = movementDtoMapper;
    }

    @Transactional
    public InventoryMovementResponse execute(RegisterInventoryMovementCommand command) {
        // 1. Validar existencia del producto
        if (!productRepository.findById(command.productId()).isPresent()) {
            throw new ProductNotFoundException(command.productId());
        }

        // 2. Obtener y actualizar el Stock en el dominio
        Stock stock = stockRepository.findByProductId(command.productId())
                .orElseThrow(() -> new StockNotFoundException(command.productId()));

        switch (command.type().toUpperCase()) {
            case "IN" -> stock.addQuantity(command.quantity());
            case "OUT" -> stock.removeQuantity(command.quantity());
            case "ADJUSTMENT" -> {
                // En un ajuste se asigna o recalcula según la lógica del negocio
                if (command.quantity() < 0) {
                    stock.removeQuantity(Math.abs(command.quantity()));
                } else {
                    stock.addQuantity(command.quantity());
                }
            }
            default -> throw new InvalidInventoryMovementTypeException(command.type());
        }

        // Guardar el stock actualizado
        stockRepository.save(stock);

        // 3. Crear y guardar el registro de movimiento
        InventoryMovement movement = InventoryMovement.builder()
                .id(UUID.randomUUID().toString())
                .productId(command.productId())
                .type(command.type().toUpperCase())
                .quantity(command.quantity())
                .reason(command.reason())
                .userId(command.userId())
                .createdAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();
        InventoryMovement savedMovement = movementRepository.save(movement);
        return movementDtoMapper.toResponse(savedMovement);
    }
}
