package com.alexlizzt.inventory_service.domain.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @Test
    @DisplayName("Debe actualizar todos los detalles correctamente cuando los valores son válidos")
    void shouldUpdateAllDetailsSuccessfully() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        Product product = Product.builder()
                .id("1")
                .sku("SKU-001")
                .name("Old Name")
                .description("Old Desc")
                .price(new BigDecimal("10.00"))
                .categoryId("cat-1")
                .active(false)
                .createdAt(oldDate)
                .updatedAt(oldDate)
                .build();

        // Act
        product.updateDetails(
                "New Name", 
                "New Desc", 
                new BigDecimal("25.50"), 
                "cat-2", 
                true
        );

        // Assert
        assertThat(product.getName()).isEqualTo("New Name");
        assertThat(product.getDescription()).isEqualTo("New Desc");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(product.getCategoryId()).isEqualTo("cat-2");
        assertThat(product.getActive()).isTrue();
        assertThat(product.getUpdatedAt()).isAfter(oldDate);
    }

    @Test
    @DisplayName("No debe actualizar los campos si se envían valores nulos o en blanco")
    void shouldNotUpdateFieldsWhenNullOrBlank() {
        // Arrange
        Product product = Product.builder()
                .id("1")
                .name("Original Name")
                .description("Original Desc")
                .price(new BigDecimal("100.00"))
                .categoryId("cat-original")
                .active(true)
                .build();

        // Act (enviando nulls, strings vacíos o en blanco)
        product.updateDetails("   ", null, null, "   ", null);

        // Assert
        assertThat(product.getName()).isEqualTo("Original Name");
        assertThat(product.getDescription()).isEqualTo("Original Desc");
        assertThat(product.getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(product.getCategoryId()).isEqualTo("cat-original");
        assertThat(product.getActive()).isTrue();
        assertThat(product.getUpdatedAt()).isNotNull(); // updatedAt se marca siempre
    }

    @Test
    @DisplayName("No debe actualizar el precio si es menor a cero (negativo)")
    void shouldNotUpdatePriceWhenNegative() {
        // Arrange
        BigDecimal originalPrice = new BigDecimal("50.00");
        Product product = Product.builder()
                .id("1")
                .price(originalPrice)
                .build();

        // Act (intentando poner un precio negativo y un precio válido cero)
        product.updateDetails(null, null, new BigDecimal("-10.00"), null, null);

        // Assert
        assertThat(product.getPrice()).isEqualByComparingTo(originalPrice);

        // Act (probando con cero, que sí debería permitirse por la condición >= 0)
        product.updateDetails(null, null, BigDecimal.ZERO, null, null);
        assertThat(product.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}