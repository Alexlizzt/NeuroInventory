package com.alexlizzt.inventory_service.application.usecase;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class CreateProductUseCase {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final Clock clock;

    @Transactional
    public Product execute(String categoryId, String sku, String name, String description, BigDecimal price, int initialStock, int minStock) {
        // 1. Validar que la categoría exista
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("La categoría con ID " + categoryId + " no existe"));

        // 2. Validar que el SKU no esté duplicado
        if (productRepository.findBySku(sku).isPresent()) {
            throw new IllegalArgumentException("Ya existe un producto con el SKU: " + sku);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        String productId = UUID.randomUUID().toString();

        // 3. Crear la entidad de dominio Product
        Product product = new Product(
                productId,
                categoryId,
                sku,
                name,
                description,
                price,
                true, // active por defecto
                now,
                now
        );

        // 4. Crear su respectivo Stock inicial
        Stock stock = new Stock(
                productId,
                initialStock,
                minStock,
                now
        );

        // 5. Guardar ambos utilizando los repositorios del dominio
        Product savedProduct = productRepository.save(product);
        stockRepository.save(stock);

        return savedProduct;
    }
}
