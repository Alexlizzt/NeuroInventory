package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;


import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.StockEntity;


class StockJpaRepositoryTest extends AbstractJpaTest  {

    @Autowired
    private StockJpaRepository stockJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeEach
    void setUp() {
        stockJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();

        CategoryEntity category = new CategoryEntity(
                "category-1",
                "Electronics",
                "Electronic products",
                null,
                null
        );

        categoryJpaRepository.save(category);

        ProductEntity product1 = new ProductEntity(
                "product-1",
                "category-1",
                "SKU-001",
                "iPhone 15",
                "Apple smartphone",
                new BigDecimal("999.99"),
                true,
                null,
                null
        );

        ProductEntity product2 = new ProductEntity(
                "product-2",
                "category-1",
                "SKU-002",
                "Samsung Galaxy",
                "Samsung smartphone",
                new BigDecimal("799.99"),
                true,
                null,
                null
        );

        ProductEntity product3 = new ProductEntity(
                "product-3",
                "category-1",
                "SKU-003",
                "Google Pixel",
                "Google smartphone",
                new BigDecimal("699.99"),
                true,
                null,
                null
        );

        ProductEntity product4 = new ProductEntity(
                "product-4",
                "category-1",
                "SKU-004",
                "MacBook Air",
                "Apple laptop",
                new BigDecimal("1299.99"),
                true,
                null,
                null
        );

        productJpaRepository.saveAll(List.of(
                product1,
                product2,
                product3,
                product4
        ));

        StockEntity stock1 = new StockEntity(
                "product-1",
                5,
                10,
                null
        );

        StockEntity stock2 = new StockEntity(
                "product-2",
                10,
                10,
                null
        );

        StockEntity stock3 = new StockEntity(
                "product-3",
                20,
                10,
                null
        );

        StockEntity stock4 = new StockEntity(
                "product-4",
                2,
                5,
                null
        );

        stockJpaRepository.saveAll(List.of(
                stock1,
                stock2,
                stock3,
                stock4
        ));
    }

    @Test
    void shouldFindProductsWithLowStock() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<StockEntity> result =
                stockJpaRepository.findLowStock(pageable);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(StockEntity::getProductId)
                .containsExactlyInAnyOrder(
                        "product-1",
                        "product-2",
                        "product-4"
                );
    }

    @Test
    void shouldIncludeStockWhenQuantityEqualsMinStock() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<StockEntity> result =
                stockJpaRepository.findLowStock(pageable);

        assertThat(result.getContent())
                .extracting(StockEntity::getProductId)
                .contains("product-2");
    }

    @Test
    void shouldNotIncludeStockWhenQuantityIsGreaterThanMinStock() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<StockEntity> result =
                stockJpaRepository.findLowStock(pageable);

        assertThat(result.getContent())
                .extracting(StockEntity::getProductId)
                .doesNotContain("product-3");
    }

    @Test
    void shouldRespectPagination() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<StockEntity> result =
                stockJpaRepository.findLowStock(pageable);

        assertThat(result.getContent())
                .hasSize(2);

        assertThat(result.getTotalElements())
                .isEqualTo(3);

        assertThat(result.getTotalPages())
                .isEqualTo(2);

        assertThat(result.isFirst())
                .isTrue();

        assertThat(result.hasNext())
                .isTrue();
    }

    @Test
    void shouldReturnSecondPageWhenRequested() {
        Pageable pageable = PageRequest.of(1, 2);

        Page<StockEntity> result =
                stockJpaRepository.findLowStock(pageable);

        assertThat(result.getContent())
                .hasSize(1);

        assertThat(result.getTotalElements())
                .isEqualTo(3);

        assertThat(result.isLast())
                .isTrue();

        assertThat(result.hasNext())
                .isFalse();
    }
}