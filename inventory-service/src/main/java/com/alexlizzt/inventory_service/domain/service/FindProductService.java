package com.alexlizzt.inventory_service.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.usecase.FindProductUseCase;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class FindProductService implements FindProductUseCase {
    private final ProductRepository productRepository;
    private final ProductDtoMapper productDtoMapper;

    public FindProductService(ProductRepository productRepository, ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.productDtoMapper = productDtoMapper;
    }

    @Override 
    @Transactional(readOnly = true)
    public ProductResponse execute(String id) {
        return productRepository.findById(id)
                .map(productDtoMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
}
