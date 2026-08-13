package com.alexlizzt.inventory_service.application.usecase;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.command.RegisterInventoryMovementCommand;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterInventoryMovementUseCase {

    private final Clock clock;
    private final InventoryMovementRepository movementRepository;
    private final StockRepository stockRepository;

    @Transactional
    public InventoryMovement execute(RegisterInventoryMovementCommand command) {
        // 1. Buscar el stock actual del producto
        Stock stock = stockRepository.findByProductId(command.productId())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró stock para el producto con ID: " + command.productId()));

        // 2. Calcular la nueva cantidad según el tipo de movimiento y obtener la hora segura con el clock
        int currentQuantity = stock.getQuantity();
        int newQuantity;
        LocalDateTime now = LocalDateTime.now(clock);

        switch (command.type()) {
            case IN:
                newQuantity = currentQuantity + command.quantity();
                break;
            case OUT:
                newQuantity = currentQuantity - command.quantity();
                if (newQuantity < 0) {
                    throw new IllegalArgumentException("Stock insuficiente. Cantidad actual: " + currentQuantity + ", solicitada: " + command.quantity());
                }
                break;
            case ADJUSTMENT:
                // En un ajuste, la cantidad reportada representa el nuevo stock absoluto o el valor directo
                newQuantity = command.quantity(); 
                break;
            default:
                throw new IllegalArgumentException("Tipo de movimiento no soportado: " + command.type());
        }

        // 3. Actualizar el stock utilizando el método de dominio
        stock.update(newQuantity, stock.getMinStock(), now);
        stockRepository.save(stock);

        // 4. Crear y registrar el movimiento histórico de inventario
        InventoryMovement movement = InventoryMovement.create(
                UUID.randomUUID().toString(),
                command.productId(),
                command.type(),
                command.quantity(),
                command.reason(),
                command.userId(),
                now
        );

        return movementRepository.save(movement);
    }
}
