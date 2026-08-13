package com.alexlizzt.inventory_service.application.usecase;

import java.time.Clock;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateStockUseCase {
    
    private final Clock clock;
    private final StockRepository stockRepository;

    @Transactional
    public Stock execute(String productId, int newQuantity, int newMinStock) {
        // 1. Buscar el stock actual del producto
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró stock para el producto con ID: " + productId));

        // 2. Obtener la hora segura con el clock
        LocalDateTime now = LocalDateTime.now(clock);

        // 3. Aplicar la regla de negocio de actualización del dominio
        stock.update(newQuantity, newMinStock, now);

        // 4. Persistir los cambios
        return stockRepository.save(stock);
    }
}
