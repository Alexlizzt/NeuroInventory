package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.command.UpdateProductCommand;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class UpdateProductUseCase {

    private final ProductRepository productRepository;
    private final ProductDtoMapper productDtoMapper;

    public UpdateProductUseCase(ProductRepository productRepository, ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.productDtoMapper = productDtoMapper;
    }

    @Transactional
    public ProductResponse execute(UpdateProductCommand command) {
        Product existingProduct = productRepository.findById(command.id())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + command.id()));

        // Delegamos la mutación al método explícito de la entidad de dominio
        existingProduct.updateDetails(
            command.name(),
            command.description(),
            command.price(),
            command.categoryId(),
            command.active()
        );

        Product updatedProduct = productRepository.save(existingProduct);
        return productDtoMapper.toResponse(updatedProduct);
    } 
}
