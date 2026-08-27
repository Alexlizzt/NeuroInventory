package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public DeleteProductUseCase(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public void execute(String id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
    }
}
