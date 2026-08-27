package com.alexlizzt.inventory_service.infraestructure.persistence.adapter;

import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;
import com.alexlizzt.inventory_service.infraestructure.persistence.mapper.StockEntityMapper;
import com.alexlizzt.inventory_service.infraestructure.persistence.springdata.StockJpaRepository;

@Repository
public class StockRepositoryAdapter implements StockRepository {

    private final StockJpaRepository stockJpaRepository;
    private final StockEntityMapper mapper;

    public StockRepositoryAdapter(StockJpaRepository stockJpaRepository, StockEntityMapper mapper) {
        this.stockJpaRepository = stockJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Stock save(Stock stock) {
        var entity = mapper.toEntity(stock);
        var savedEntity = stockJpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Stock> findByProductId(String productId) {
        return stockJpaRepository.findById(productId).map(mapper::toDomain);
    }

    @Override
    public PageResult<Stock> findLowStockPaged(PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        var page = stockJpaRepository.findLowStock(pageable);

        var content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

    @Override
    public PageResult<Stock> findAllPaged(PageQuery pageQuery) {
        Pageable pageable = PageRequest.of(pageQuery.page(), pageQuery.size());
        var page = stockJpaRepository.findAll(pageable);

        var content = page.getContent().stream().map(mapper::toDomain).toList();
        return new PageResult<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }

}
