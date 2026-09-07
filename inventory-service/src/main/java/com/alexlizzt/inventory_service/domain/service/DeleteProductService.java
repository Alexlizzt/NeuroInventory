package com.alexlizzt.inventory_service.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.DeleteProductUseCase;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class DeleteProductService implements DeleteProductUseCase {
    private final ProductRepository productRepository;

    public DeleteProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override 
    @Transactional
    public void execute(String id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
    }
}
