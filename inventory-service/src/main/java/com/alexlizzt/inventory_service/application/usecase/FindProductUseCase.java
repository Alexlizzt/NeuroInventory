package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindProductUseCase {
    
    private final ProductRepository productRepository;

    /**
     * Buscar un producto por su ID único.
     */
    @Transactional(readOnly = true)
    public Product executeById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));
    }

    /**
     * Buscar un producto por su SKU (código único de inventario).
     */
    @Transactional(readOnly = true)
    public Product executeBySku(String sku) {
        return productRepository.findBySku(sku)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con SKU: " + sku));
    }
}
