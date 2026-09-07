package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.SemanticSearchProductResponse;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.SemanticMatch;
import com.alexlizzt.inventory_service.domain.port.AiServicePort;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchProductsSemanticallyServiceTest {

    @Mock
    private AiServicePort aiServicePort;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SearchProductsSemanticallyService searchProductsSemanticallyService;

    @Test
    @DisplayName("Debe retornar lista vacía si la consulta es nula o en blanco")
    void shouldReturnEmptyListWhenQueryIsNullOrEmptyOrBlank() {
        // Act & Assert
        assertThat(searchProductsSemanticallyService.execute(null, 5)).isEmpty();
        assertThat(searchProductsSemanticallyService.execute("   ", 5)).isEmpty();

        // Verify interactions - ensure dependencies are never called
        verifyNoInteractions(aiServicePort, productRepository);
    }

    @Test
    @DisplayName("Debe retornar lista vacía si el servicio de IA no encuentra coincidencias")
    void shouldReturnEmptyListWhenAiServiceReturnsNoMatches() {
        // Arrange
        String query = "gaming laptop";
        when(aiServicePort.searchSemantically(query, 5)).thenReturn(List.of());

        // Act
        List<SemanticSearchProductResponse> result = searchProductsSemanticallyService.execute(query, 5);

        // Assert
        assertThat(result).isEmpty();

        // Verify interactions
        verify(aiServicePort).searchSemantically(query, 5);
        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("Debe buscar y combinar exitosamente las coincidencias semánticas con los productos de la BD")
    void shouldSearchAndCombineSemanticMatchesSuccessfully() {
        // Arrange
        String query = "laptop";
        int limit = 5;

        SemanticMatch match1 = new SemanticMatch("prod-1", 0.95, "Great match");
        SemanticMatch match2 = new SemanticMatch("prod-2", 0.85, "Good match");

        when(aiServicePort.searchSemantically(query, limit)).thenReturn(List.of(match1, match2));

        Product product1 = Product.builder()
                .id("prod-1")
                .categoryId("cat-1")
                .sku("SKU-1")
                .name("Laptop Pro")
                .description("High-end laptop")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .build();

        Product product2 = Product.builder()
                .id("prod-2")
                .categoryId("cat-1")
                .sku("SKU-2")
                .name("Laptop Air")
                .description("Lightweight laptop")
                .price(new BigDecimal("1100.00"))
                .active(true)
                .build();

        when(productRepository.findAllByIds(List.of("prod-1", "prod-2"))).thenReturn(List.of(product1, product2));

        // Act
        List<SemanticSearchProductResponse> result = searchProductsSemanticallyService.execute(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        assertThat(result.get(0).id()).isEqualTo("prod-1");
        assertThat(result.get(0).name()).isEqualTo("Laptop Pro");
        assertThat(result.get(0).score()).isEqualTo(0.95);
        assertThat(result.get(0).rationale()).isEqualTo("Great match");

        assertThat(result.get(1).id()).isEqualTo("prod-2");
        assertThat(result.get(1).name()).isEqualTo("Laptop Air");
        assertThat(result.get(1).score()).isEqualTo(0.85);
        assertThat(result.get(1).rationale()).isEqualTo("Good match");

        // Verify interactions
        verify(aiServicePort).searchSemantically(query, limit);
        verify(productRepository).findAllByIds(List.of("prod-1", "prod-2"));
    }

    @Test
    @DisplayName("Debe filtrar aquellos matches cuyos IDs no existan en la base de datos relacional")
    void shouldFilterOutMatchesNotInDatabase() {
        // Arrange
        String query = "laptop";
        int limit = 5;

        SemanticMatch match1 = new SemanticMatch("prod-1", 0.95, "Match exists");
        SemanticMatch match2 = new SemanticMatch("prod-999", 0.80, "Match does not exist in DB");

        when(aiServicePort.searchSemantically(query, limit)).thenReturn(List.of(match1, match2));

        Product product1 = Product.builder()
                .id("prod-1")
                .name("Laptop Pro")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .build();

        // Solo retorna el producto 1, el prod-999 fue borrado o no existe
        when(productRepository.findAllByIds(List.of("prod-1", "prod-999"))).thenReturn(List.of(product1));

        // Act
        List<SemanticSearchProductResponse> result = searchProductsSemanticallyService.execute(query, limit);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("prod-1");

        // Verify interactions
        verify(aiServicePort).searchSemantically(query, limit);
        verify(productRepository).findAllByIds(List.of("prod-1", "prod-999"));
    }
}