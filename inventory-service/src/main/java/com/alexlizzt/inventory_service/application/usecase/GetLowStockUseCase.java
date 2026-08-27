package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.application.mapper.StockDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

@Service
public class GetLowStockUseCase {
    private final StockRepository stockRepository;
    private final StockDtoMapper stockDtoMapper;

    public GetLowStockUseCase(StockRepository stockRepository, StockDtoMapper stockDtoMapper) {
        this.stockRepository = stockRepository;
        this.stockDtoMapper = stockDtoMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<StockResponse> execute(PageQuery pageQuery) {
        PageResult<Stock> domainPage = stockRepository.findLowStockPaged(pageQuery);

        List<StockResponse> content = domainPage.content().stream()
                .map(stockDtoMapper::toResponse)
                .toList();

        return new PageResult<>(
            content,
            domainPage.pageNumber(),
            domainPage.pageSize(),
            domainPage.totalElements(),
            domainPage.totalPages()
        );
    }
}
