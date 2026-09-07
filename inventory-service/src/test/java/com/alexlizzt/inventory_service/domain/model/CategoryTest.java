package com.alexlizzt.inventory_service.domain.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    @DisplayName("Debe actualizar nombre y descripción correctamente cuando ambos son válidos")
    void shouldUpdateDetailsSuccessfully() {
        // Arrange
        LocalDateTime oldDate = LocalDateTime.of(2023, 1, 1, 0, 0);
        Category category = Category.builder()
                .id("1")
                .name("Old Name")
                .description("Old Description")
                .createdAt(oldDate)
                .updatedAt(oldDate)
                .build();

        // Act
        category.updateDetails("New Name", "  New Description  ");

        // Assert
        assertThat(category.getName()).isEqualTo("New Name");
        assertThat(category.getDescription()).isEqualTo("New Description"); // Verifica que aplique el trim()
        assertThat(category.getUpdatedAt()).isAfter(oldDate); // Verifica que se actualice la fecha
    }

    @Test
    @DisplayName("No debe actualizar el nombre si es nulo o en blanco, pero sí la descripción")
    void shouldNotUpdateNameWhenNullOrBlank() {
        // Arrange
        Category category = Category.builder()
                .id("1")
                .name("Original Name")
                .description("Original Description")
                .build();

        // Act
        category.updateDetails(null, "Updated Description");

        // Assert
        assertThat(category.getName()).isEqualTo("Original Name");
        assertThat(category.getDescription()).isEqualTo("Updated Description");
    }

    @Test
    @DisplayName("No debe actualizar la descripción si es nula, pero sí el nombre")
    void shouldNotUpdateDescriptionWhenNull() {
        // Arrange
        Category category = Category.builder()
                .id("1")
                .name("Original Name")
                .description("Original Description")
                .build();

        // Act
        category.updateDetails("New Name", null);

        // Assert
        assertThat(category.getName()).isEqualTo("New Name");
        assertThat(category.getDescription()).isEqualTo("Original Description");
    }

    @Test
    @DisplayName("Debe validar el funcionamiento del Builder y los Getters generados por Lombok")
    void shouldTestLombokAnnotations() {
        // Arrange & Act
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        Category category = Category.builder()
                .id("123")
                .name("Electronics")
                .description("Gadgets and devices")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Assert
        assertThat(category.getId()).isEqualTo("123");
        assertThat(category.getName()).isEqualTo("Electronics");
        assertThat(category.getDescription()).isEqualTo("Gadgets and devices");
        assertThat(category.getCreatedAt()).isEqualTo(now);
        assertThat(category.getUpdatedAt()).isEqualTo(now);
    }
}