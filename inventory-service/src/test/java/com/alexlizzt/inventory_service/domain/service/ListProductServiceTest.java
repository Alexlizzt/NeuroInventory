package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.mapper.ProductDtoMapper;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductDtoMapper productDtoMapper;

    @InjectMocks
    private ListProductService listProductService;

    @Test
    @DisplayName("Debe retornar una página paginada de productos mapeados exitosamente")
    void shouldReturnPagedProductsSuccessfully() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        Product product = Product.builder()
                .id("prod-1")
                .sku("SKU-1")
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1200.00"))
                .active(true)
                .build();

        PageResult<Product> domainPage = new PageResult<>(
                List.of(product),
                0,
                10,
                1L,
                1
        );

        when(productRepository.findAllPaged(pageQuery)).thenReturn(domainPage);

        ProductResponse productResponse = new ProductResponse(
                "prod-1", "SKU-1", "Laptop", "Gaming laptop",
                new BigDecimal("1200.00"), true, "cat-1", null, null
        );
        when(productDtoMapper.toResponse(product)).thenReturn(productResponse);

        // Act
        PageResult<ProductResponse> result = listProductService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).id()).isEqualTo("prod-1");
        assertThat(result.content().get(0).sku()).isEqualTo("SKU-1");
        assertThat(result.content().get(0).name()).isEqualTo("Laptop");
        assertThat(result.pageNumber()).isZero();
        assertThat(result.pageSize()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(1L);
        assertThat(result.totalPages()).isEqualTo(1);

        // Verify interactions
        verify(productRepository).findAllPaged(pageQuery);
        verify(productDtoMapper).toResponse(product);
    }

    @Test
    @DisplayName("Debe retornar una página vacía si no existen productos registrados")
    void shouldReturnEmptyPageWhenNoProductsExist() {
        // Arrange
        PageQuery pageQuery = new PageQuery(0, 10);

        PageResult<Product> emptyPage = new PageResult<>(
                List.of(),
                0,
                10,
                0L,
                0
        );

        when(productRepository.findAllPaged(pageQuery)).thenReturn(emptyPage);

        // Act
        PageResult<ProductResponse> result = listProductService.execute(pageQuery);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();

        // Verify interactions
        verify(productRepository).findAllPaged(pageQuery);
    }
}