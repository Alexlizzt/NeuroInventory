package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class ListProductsUseCase {
    private final ProductRepository productRepository;
    private final ProductDtoMapper productDtoMapper;

    public ListProductsUseCase(ProductRepository productRepository, ProductDtoMapper productDtoMapper) {
        this.productRepository = productRepository;
        this.productDtoMapper = productDtoMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<ProductResponse> execute(PageQuery pageQuery) {
        PageResult<Product> domainPage = productRepository.findAllPaged(pageQuery);

        List<ProductResponse> content = domainPage.content().stream()
                .map(productDtoMapper::toResponse)
                .toList();

        return new PageResult<>(
            content,
            domainPage.pageNumber(),
            domainPage.pageSize(),
            domainPage.totalElements(),
            domainPage.totalPages()
        );
    }
}
