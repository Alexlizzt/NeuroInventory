package com.alexlizzt.inventory_service.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.usecase.query.PageQuery;
import com.alexlizzt.inventory_service.application.usecase.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchProductsUseCase {
    private final ProductRepository productRepository;

    /**
     * Obtiene el listado completo de productos registrados en el inventario.
     */
    @Transactional(readOnly = true)
    public PageResult<Product> execute(PageQuery pageQuery) {
        // Validaciones básicas de negocio para los parámetros de paginación si se requiere
        int page = pageQuery.page() < 0 ? 0 : pageQuery.page();
        int size = pageQuery.size() <= 0 ? 10 : pageQuery.size();
        
        PageQuery sanitizedQuery = new PageQuery(page, size, pageQuery.sortBy(), pageQuery.direction());

        return productRepository.findAllPaged(sanitizedQuery);
    }
}
