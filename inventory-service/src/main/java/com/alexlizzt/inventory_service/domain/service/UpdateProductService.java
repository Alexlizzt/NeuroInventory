package com.alexlizzt.inventory_service.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.usecase.UpdateProductUseCase;
import com.alexlizzt.inventory_service.application.command.UpdateProductCommand;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.CategoryRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class UpdateProductService implements UpdateProductUseCase {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductDtoMapper productDtoMapper;

    public UpdateProductService(ProductRepository productRepository, CategoryRepository categoryRepository, ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productDtoMapper = productDtoMapper;
    }

    @Override 
    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
       // 1. Verificar existencia del producto
        Product existingProduct = productRepository.findById(command.id())
                .orElseThrow(() -> new ProductNotFoundException(command.id()));

        // 2. Si se cambia de categoría, validar que exista
        if (command.categoryId() != null && !command.categoryId().equalsIgnoreCase(existingProduct.getCategoryId())) {
            boolean categoryExists = categoryRepository.findById(command.categoryId()).isPresent();
            if (!categoryExists) {
                throw new CategoryNotFoundException(command.categoryId());
            }
        }

        // 3. Invocación exacta de TU método de dominio updateDetails
        existingProduct.updateDetails(
            command.name(),
            command.description(),
            command.price(),
            command.categoryId(),
            command.active()
        );

        // 4. Persistir cambios
        Product updatedProduct = productRepository.save(existingProduct);

        // 5. Retornar DTO mapeado
        return productDtoMapper.toResponse(updatedProduct);
    }
}
