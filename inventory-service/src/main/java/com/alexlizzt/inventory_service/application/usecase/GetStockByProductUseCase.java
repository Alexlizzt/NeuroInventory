package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetStockByProductUseCase {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public Stock execute(String productId) {
        // Consulta el stock utilizando el repositorio y valida su existencia
        return stockRepository.findByProductId(productId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró información de stock para el producto con ID: " + productId));
    }
}
