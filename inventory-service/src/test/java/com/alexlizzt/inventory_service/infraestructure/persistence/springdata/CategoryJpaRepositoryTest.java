package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import com.alexlizzt.inventory_service.infraestructure.persistence.entity.CategoryEntity;

class CategoryJpaRepositoryTest extends AbstractJpaTest {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Debe encontrar una categoría por su nombre correctamente")
    void shouldFindCategoryByName() {
        // Given
        CategoryEntity category = new CategoryEntity();
        category.setId("cat-1");
        category.setName("Electrónica");

        entityManager.persistAndFlush(category);

        // When
        Optional<CategoryEntity> found =
                categoryJpaRepository.findByName("Electrónica");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electrónica");
    }

    @Test
    @DisplayName("Debe retornar Optional vacío cuando el nombre de la categoría no existe")
    void shouldReturnEmptyWhenCategoryNameNotFound() {
        // When
        Optional<CategoryEntity> found =
                categoryJpaRepository.findByName("NoExiste");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Debe retornar true si la categoría existe por su nombre")
    void shouldReturnTrueWhenCategoryExistsByName() {
        // Given
        CategoryEntity category = new CategoryEntity();
        category.setId("cat-2");
        category.setName("Hogar");

        entityManager.persistAndFlush(category);

        // When
        boolean exists = categoryJpaRepository.existsByName("Hogar");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Debe retornar false si la categoría no existe por su nombre")
    void shouldReturnFalseWhenCategoryDoesNotExistByName() {
        // When
        boolean exists =
                categoryJpaRepository.existsByName("NoExiste");

        // Then
        assertThat(exists).isFalse();
    }
}