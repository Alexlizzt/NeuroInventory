package com.alexlizzt.inventory_service.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.port.AiServicePort;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ReindexProductsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private AiServicePort aiServicePort;

    @InjectMocks
    private ReindexProductsService reindexProductsService;

    @Test
    @DisplayName("Reindexa todos los productos y retorna la cantidad procesada")
    void shouldReindexAllProducts() {
        Product p1 = Product.builder().id("p1").name("Laptop").build();
        Product p2 = Product.builder().id("p2").name("Mouse").build();
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(aiServicePort.indexProduct(any())).thenReturn(true);

        int indexed = reindexProductsService.execute();

        assertThat(indexed).isEqualTo(2);
        verify(aiServicePort).indexProduct(p1);
        verify(aiServicePort).indexProduct(p2);
    }

    @Test
    @DisplayName("Retorna cero y no llama al AI Service cuando no hay productos")
    void shouldReturnZeroWhenNoProducts() {
        when(productRepository.findAll()).thenReturn(List.of());

        int indexed = reindexProductsService.execute();

        assertThat(indexed).isZero();
        verify(aiServicePort, never()).indexProduct(any());
    }

    @Test
    @DisplayName("Propaga un fallo si algún producto no pudo indexarse")
    void shouldThrowWhenSomeProductFails() {
        Product p1 = Product.builder().id("p1").name("Laptop").build();
        Product p2 = Product.builder().id("p2").name("Mouse").build();
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));
        when(aiServicePort.indexProduct(p1)).thenReturn(true);
        when(aiServicePort.indexProduct(p2)).thenReturn(false);

        assertThatThrownBy(() -> reindexProductsService.execute())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("1 producto(s)");
    }
}
