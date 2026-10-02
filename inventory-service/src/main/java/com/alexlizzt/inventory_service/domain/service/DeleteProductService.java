package com.alexlizzt.inventory_service.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.DeleteProductUseCase;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.port.AiServicePort;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class DeleteProductService implements DeleteProductUseCase {
    private final ProductRepository productRepository;
    private final AiServicePort aiServicePort;

    public DeleteProductService(ProductRepository productRepository, AiServicePort aiServicePort) {
        this.productRepository = productRepository;
        this.aiServicePort = aiServicePort;
    }

    @Override 
    @Transactional
    public void execute(String id) {
        if (!productRepository.findById(id).isPresent()) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);

        // Limpiar el embedding asociado (best-effort)
        aiServicePort.deleteProductIndex(id);
    }
}
