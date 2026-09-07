package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryMovementDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetInventoryHistoryServiceTest {

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementDtoMapper movementDtoMapper;

    @InjectMocks
    private GetInventoryHistoryService getInventoryHistoryService;

    @Test
    @DisplayName("Debe retornar el historial de movimientos de inventario cuando el producto existe")
    void shouldReturnInventoryHistorySuccessfullyWhenProductExists() {
        // Arrange
        String productId = "prod-123";

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));

        InventoryMovement movement = InventoryMovement.builder()
                .id("mov-1")
                .productId(productId)
                .type("IN")
                .quantity(50)
                .reason("Restock")
                .userId("user-1")
                .createdAt(LocalDateTime.now())
                .build();

        when(movementRepository.findByProductId(productId)).thenReturn(List.of(movement));

        InventoryMovementResponse responseDto = new InventoryMovementResponse(
                "mov-1", productId, "IN", 50, "Restock", "user-1", LocalDateTime.now()
        );
        when(movementDtoMapper.toResponse(movement)).thenReturn(responseDto);

        // Act
        List<InventoryMovementResponse> result = getInventoryHistoryService.execute(productId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("mov-1");
        assertThat(result.get(0).productId()).isEqualTo(productId);
        assertThat(result.get(0).quantity()).isEqualTo(50);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(movementRepository).findByProductId(productId);
        verify(movementDtoMapper).toResponse(movement);
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si el producto no existe al consultar el historial")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        String productId = "prod-999";

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getInventoryHistoryService.execute(productId))
                .isInstanceOf(ProductNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(movementRepository, never()).findByProductId(any());
        verify(movementDtoMapper, never()).toResponse(any());
    }
}
