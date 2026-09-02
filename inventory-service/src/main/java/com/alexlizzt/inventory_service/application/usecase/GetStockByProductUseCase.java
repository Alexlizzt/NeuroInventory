package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.domain.exception.StockNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

@Service
public class GetStockByProductUseCase {

    private final StockRepository stockRepository;

    public GetStockByProductUseCase(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional(readOnly = true)
    public StockResponse execute(String productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new StockNotFoundException(productId));

        return new StockResponse(
                stock.getProductId(),
                stock.getQuantity(),
                stock.getMinStock(),
                stock.isLowStock(),
                stock.getUpdatedAt()
                );
    }
}
