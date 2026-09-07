package com.alexlizzt.inventory_service.domain.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SemanticMatchTest {

    @Test
    @DisplayName("Debe crear la instancia usando el Builder y recuperar los valores con los Getters")
    void shouldCreateUsingBuilder() {
        // Arrange
        String productId = "prod-123";
        Double score = 0.95;
        String rationale = "High semantic similarity based on category and tags.";

        // Act
        SemanticMatch match = SemanticMatch.builder()
                .productId(productId)
                .score(score)
                .rationale(rationale)
                .build();

        // Assert
        assertThat(match).isNotNull();
        assertThat(match.getProductId()).isEqualTo(productId);
        assertThat(match.getScore()).isEqualTo(score);
        assertThat(match.getRationale()).isEqualTo(rationale);
    }

    @Test
    @DisplayName("Debe funcionar correctamente con los Setters generados por @Data")
    void shouldTestSetters() {
        // Arrange
        SemanticMatch match = new SemanticMatch();

        // Act
        match.setProductId("prod-789");
        match.setScore(0.85);
        match.setRationale("Good match");

        // Assert
        assertThat(match.getProductId()).isEqualTo("prod-789");
        assertThat(match.getScore()).isEqualTo(0.85);
        assertThat(match.getRationale()).isEqualTo("Good match");
    }

    @Test
    @DisplayName("Debe validar equals, hashCode y toString generados por @Data")
    void shouldTestEqualsHashCodeAndToString() {
        // Arrange
        SemanticMatch match1 = new SemanticMatch("prod-1", 0.9, "Exact match");
        SemanticMatch match2 = new SemanticMatch("prod-1", 0.9, "Exact match");
        SemanticMatch match3 = new SemanticMatch("prod-2", 0.5, "Low match");

        // Assert equals & hashCode
        assertThat(match1).isEqualTo(match2);
        assertThat(match1.hashCode()).hasSameHashCodeAs(match2.hashCode());
        assertThat(match1).isNotEqualTo(match3);

        // Assert toString contains field values
        assertThat(match1.toString())
                .contains("prod-1")
                .contains("0.9")
                .contains("Exact match");
    }
}