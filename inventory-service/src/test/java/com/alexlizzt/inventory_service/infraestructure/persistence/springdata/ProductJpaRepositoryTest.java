package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;


class ProductJpaRepositoryTest extends AbstractJpaTest {
	@Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeEach
    void setUp() {
        productJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();

        CategoryEntity electronics = new CategoryEntity(
                "category-1",
                "Electronics",
                "Electronic products",
                null,
                null
        );

        CategoryEntity shoes = new CategoryEntity(
                "category-2",
                "Shoes",
                "Shoes and footwear",
                null,
                null
        );

        categoryJpaRepository.saveAll(List.of(
                electronics,
                shoes
        ));

        ProductEntity iphone = new ProductEntity(
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

        ProductEntity samsung = new ProductEntity(
        "product-2",
        "category-1",
        "SKU-002",
        "Samsung Galaxy Phone",
        "Samsung smartphone",
        new BigDecimal("799.99"),
        true,
        null,
        null
);

        ProductEntity nike = new ProductEntity(
                "product-3",
                "category-2",
                "SKU-003",
                "Nike Air Max",
                "Running shoes",
                new BigDecimal("129.99"),
                true,
                null,
                null
        );

        productJpaRepository.saveAll(List.of(
                iphone,
                samsung,
                nike
        ));
    }

    @Test
    void shouldFindProductBySku() {
        Optional<ProductEntity> result =
                productJpaRepository.findBySku("SKU-001");

        assertThat(result).isPresent();

        ProductEntity product = result.get();

        assertThat(product.getId()).isEqualTo("product-1");
        assertThat(product.getSku()).isEqualTo("SKU-001");
        assertThat(product.getName()).isEqualTo("iPhone 15");
        assertThat(product.getCategoryId()).isEqualTo("category-1");
        assertThat(product.getPrice())
                .isEqualByComparingTo("999.99");
        assertThat(product.getActive()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenSkuDoesNotExist() {
        Optional<ProductEntity> result =
                productJpaRepository.findBySku("SKU-999");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnTrueWhenSkuExists() {
        boolean result =
                productJpaRepository.existsBySku("SKU-001");

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseWhenSkuDoesNotExist() {
        boolean result =
                productJpaRepository.existsBySku("SKU-999");

        assertThat(result).isFalse();
    }

    @Test
    void shouldFindProductsByNameContainingIgnoreCaseAndCategoryId() {
        List<ProductEntity> result =
                productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        "IPHONE",
                        "category-1"
                );

        assertThat(result)
                .hasSize(1)
                .extracting(ProductEntity::getName)
                .containsExactly("iPhone 15");
    }

    @Test
    void shouldIgnoreCaseWhenSearchingByName() {
        List<ProductEntity> result =
                productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        "sAmSuNg",
                        "category-1"
                );

        assertThat(result)
                .hasSize(1)
                .extracting(ProductEntity::getName)
                .containsExactly("Samsung Galaxy Phone");
    }

    @Test
	void shouldFindMultipleProductsByPartialNameIgnoringCaseAndCategory() {
		List<ProductEntity> result =
				productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
						"PHONE",
						"category-1"
				);

		assertThat(result)
				.hasSize(2)
				.extracting(ProductEntity::getName)
				.containsExactlyInAnyOrder(
						"iPhone 15",
						"Samsung Galaxy Phone"
				);
	}

    @Test
    void shouldFilterProductsByCategoryId() {
        List<ProductEntity> result =
                productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        "a",
                        "category-2"
                );

        assertThat(result)
                .hasSize(1)
                .extracting(ProductEntity::getName)
                .containsExactly("Nike Air Max");
    }

    @Test
    void shouldReturnEmptyWhenNameDoesNotMatch() {
        List<ProductEntity> result =
                productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        "MacBook",
                        "category-1"
                );

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCategoryDoesNotMatch() {
        List<ProductEntity> result =
                productJpaRepository.findByNameContainingIgnoreCaseAndCategoryId(
                        "iPhone",
                        "category-2"
                );

        assertThat(result).isEmpty();
    }
}