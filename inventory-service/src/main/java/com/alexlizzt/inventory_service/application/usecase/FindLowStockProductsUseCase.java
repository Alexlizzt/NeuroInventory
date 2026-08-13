package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindLowStockProductsUseCase {

    private final StockRepository stockRepository;

    @Transactional(readOnly = true)
    public PageResult<Stock> execute(PageQuery pageQuery) {
        // Sanitizar parámetros de paginación básicos
        int page = pageQuery.page() < 0 ? 0 : pageQuery.page();
        int size = pageQuery.size() <= 0 ? 10 : pageQuery.size();

        PageQuery sanitizedQuery = new PageQuery(page, size, pageQuery.sortBy(), pageQuery.direction());

        return stockRepository.findLowStockPaged(sanitizedQuery);
    }
}
