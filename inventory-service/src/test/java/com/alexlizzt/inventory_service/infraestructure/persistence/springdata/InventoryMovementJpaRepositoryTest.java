package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.InventoryMovementEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.ProductEntity;

class InventoryMovementJpaRepositoryTest extends AbstractJpaTest  {
	@Autowired
    private InventoryMovementJpaRepository inventoryMovementJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementJpaRepository.deleteAll();
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

        ProductEntity product1 = ProductEntity.builder()
                .id("product-1")
                .categoryId("category-1")
                .sku("SKU-001")
                .name("iPhone")
                .description("Apple smartphone")
                .price(new BigDecimal("999.99"))
                .active(true)
                .build();

        ProductEntity product2 = ProductEntity.builder()
                .id("product-2")
                .categoryId("category-1")
                .sku("SKU-002")
                .name("Samsung Galaxy")
                .description("Samsung smartphone")
                .price(new BigDecimal("799.99"))
                .active(true)
                .build();

        productJpaRepository.saveAll(
                List.of(product1, product2)
        );

        InventoryMovementEntity movement1 = new InventoryMovementEntity(
                "movement-1",
                "product-1",
                "IN",
                10,
                "Initial stock",
                "user-1",
                null
        );

        InventoryMovementEntity movement2 = new InventoryMovementEntity(
                "movement-2",
                "product-1",
                "OUT",
                3,
                "Customer order",
                "user-2",
                null
        );

        InventoryMovementEntity movement3 = new InventoryMovementEntity(
                "movement-3",
                "product-2",
                "IN",
                20,
                "Restock",
                "user-1",
                null
        );

        inventoryMovementJpaRepository.saveAll(
                List.of(movement1, movement2, movement3)
        );
    }

    @Test
    void shouldFindMovementsByProductId() {
        List<InventoryMovementEntity> movements =
                inventoryMovementJpaRepository.findByProductId("product-1");

        assertThat(movements).hasSize(2);
        assertThat(movements)
                .extracting(InventoryMovementEntity::getProductId)
                .containsOnly("product-1");
    }

    @Test
    void shouldFindMultipleMovementsForSameProduct() {
        List<InventoryMovementEntity> movements =
                inventoryMovementJpaRepository.findByProductId("product-1");

        assertThat(movements).hasSize(2);

        assertThat(movements)
                .extracting(InventoryMovementEntity::getId)
                .containsExactlyInAnyOrder(
                        "movement-1",
                        "movement-2"
                );
    }

    @Test
    void shouldReturnOnlyMovementsForRequestedProduct() {
        List<InventoryMovementEntity> movements =
                inventoryMovementJpaRepository.findByProductId("product-2");

        assertThat(movements).hasSize(1);
        assertThat(movements.get(0).getId()).isEqualTo("movement-3");
        assertThat(movements.get(0).getProductId()).isEqualTo("product-2");
    }

    @Test
    void shouldReturnEmptyWhenProductHasNoMovements() {
        List<InventoryMovementEntity> movements =
                inventoryMovementJpaRepository.findByProductId("product-999");

        assertThat(movements).isEmpty();
    }
}