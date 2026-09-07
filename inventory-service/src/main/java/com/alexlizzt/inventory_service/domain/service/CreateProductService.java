package com.alexlizzt.inventory_service.domain.service;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.command.CreateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.usecase.CreateProductUseCase;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.DuplicateSkuException;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;

@Service
public class CreateProductService implements CreateProductUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockRepository stockRepository;
    private final ProductDtoMapper productDtoMapper;

    public CreateProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            StockRepository stockRepository,
            ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.stockRepository = stockRepository;
        this.productDtoMapper = productDtoMapper;
    }

    @Override 
    @Transactional
    public ProductResponse execute(CreateProductCommand command) {
        // 1. Validar que la categoría exista
        if (!categoryRepository.findById(command.categoryId()).isPresent()) {
            throw new CategoryNotFoundException(command.categoryId());
        }

        // 2. Validar unicidad de SKU
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException(command.sku());
        }

        String productId = UUID.randomUUID().toString();

        // 3. Crear y guardar el producto
        Product product = Product.builder()
                .id(productId)
                .categoryId(command.categoryId())
                .sku(command.sku())
                .name(command.name())
                .description(command.description())
                .price(command.price())
                .active(true)
                .build();

        Product savedProduct = productRepository.save(product);

        // 4. Inicializar el registro de Stock correspondiente
        Stock initialStock = Stock.builder()
                .productId(productId)
                .quantity(command.initialStock())
                .minStock(command.minStock())
                .build();

        stockRepository.save(initialStock);

        // 5. Retornar DTO de Respuesta
        return productDtoMapper.toResponse(savedProduct);
    }
}
