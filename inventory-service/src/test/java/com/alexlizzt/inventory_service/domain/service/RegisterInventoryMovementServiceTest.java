package com.alexlizzt.inventory_service.domain.service;
import com.alexlizzt.inventory_service.application.command.RegisterInventoryMovementCommand;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.mapper.InventoryMovementDtoMapper;
import com.alexlizzt.inventory_service.domain.exception.InvalidInventoryMovementTypeException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.StockNotFoundException;
import com.alexlizzt.inventory_service.domain.model.InventoryMovement;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.Stock;
import com.alexlizzt.inventory_service.domain.repository.InventoryMovementRepository;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;
import com.alexlizzt.inventory_service.domain.repository.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryMovementServiceTest {

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementDtoMapper movementDtoMapper;

    @InjectMocks
    private RegisterInventoryMovementService registerInventoryMovementService;

    @Test
    @DisplayName("Debe registrar un movimiento de entrada (IN) exitosamente")
    void shouldRegisterInMovementSuccessfully() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "in", 10, "Restock", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        
        Stock stock = Stock.builder().productId(productId).quantity(20).minStock(5).build();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        InventoryMovement savedMovement = InventoryMovement.builder()
                .id("mov-1")
                .productId(productId)
                .type("IN")
                .quantity(10)
                .reason("Restock")
                .userId("user-1")
                .createdAt(LocalDateTime.now())
                .build();
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse expectedResponse = new InventoryMovementResponse(
                "mov-1", productId, "IN", 10, "Restock", "user-1", LocalDateTime.now()
        );
        when(movementDtoMapper.toResponse(savedMovement)).thenReturn(expectedResponse);

        // Act
        InventoryMovementResponse response = registerInventoryMovementService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("mov-1");
        assertThat(response.type()).isEqualTo("IN");
        assertThat(response.quantity()).isEqualTo(10);
        assertThat(stock.getQuantity()).isEqualTo(30); // Verificamos que se aplicó el incremento en el stock

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(stockRepository).findByProductId(productId);
        verify(stockRepository).save(stock);
        verify(movementRepository).save(any(InventoryMovement.class));
        verify(movementDtoMapper).toResponse(savedMovement);
    }

    @Test
    @DisplayName("Debe registrar un movimiento de salida (OUT) exitosamente")
    void shouldRegisterOutMovementSuccessfully() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "OUT", 5, "Sale", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        
        Stock stock = Stock.builder().productId(productId).quantity(20).minStock(5).build();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        InventoryMovement savedMovement = InventoryMovement.builder()
                .id("mov-2")
                .productId(productId)
                .type("OUT")
                .quantity(5)
                .reason("Sale")
                .userId("user-1")
                .createdAt(LocalDateTime.now())
                .build();
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse expectedResponse = new InventoryMovementResponse(
                "mov-2", productId, "OUT", 5, "Sale", "user-1", LocalDateTime.now()
        );
        when(movementDtoMapper.toResponse(savedMovement)).thenReturn(expectedResponse);

        // Act
        InventoryMovementResponse response = registerInventoryMovementService.execute(command);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo("OUT");
        assertThat(stock.getQuantity()).isEqualTo(15); // Verificamos que se decrementó

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(stockRepository).findByProductId(productId);
        verify(stockRepository).save(stock);
        verify(movementRepository).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Debe registrar un ajuste positivo (ADJUSTMENT) exitosamente")
    void shouldRegisterPositiveAdjustmentSuccessfully() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "adjustment", 8, "Inventory recount", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        
        Stock stock = Stock.builder().productId(productId).quantity(10).minStock(2).build();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        when(movementDtoMapper.toResponse(any())).thenReturn(new InventoryMovementResponse(
                "mov-3", productId, "ADJUSTMENT", 8, "Inventory recount", "user-1", LocalDateTime.now()
        ));

        // Act
        registerInventoryMovementService.execute(command);

        // Assert
        assertThat(stock.getQuantity()).isEqualTo(18); // Se sumó al ser mayor o igual a 0
    }

    @Test
    @DisplayName("Debe registrar un ajuste negativo (ADJUSTMENT) exitosamente")
    void shouldRegisterNegativeAdjustmentSuccessfully() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "ADJUSTMENT", -4, "Damaged goods", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        
        Stock stock = Stock.builder().productId(productId).quantity(10).minStock(2).build();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(new InventoryMovement());
        when(movementDtoMapper.toResponse(any())).thenReturn(new InventoryMovementResponse(
                "mov-4", productId, "ADJUSTMENT", -4, "Damaged goods", "user-1", LocalDateTime.now()
        ));

        // Act
        registerInventoryMovementService.execute(command);

        // Assert
        assertThat(stock.getQuantity()).isEqualTo(6); // Se restó el valor absoluto de -4
    }

    @Test
    @DisplayName("Debe lanzar ProductNotFoundException si el producto no existe")
    void shouldThrowExceptionWhenProductNotFound() {
        // Arrange
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                "prod-999", "IN", 10, "Restock", "user-1"
        );

        when(productRepository.findById("prod-999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerInventoryMovementService.execute(command))
                .isInstanceOf(ProductNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById("prod-999");
        verify(stockRepository, never()).findByProductId(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar StockNotFoundException si no existe registro de stock para el producto")
    void shouldThrowExceptionWhenStockNotFound() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "IN", 10, "Restock", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> registerInventoryMovementService.execute(command))
                .isInstanceOf(StockNotFoundException.class);

        // Verify interactions
        verify(productRepository).findById(productId);
        verify(stockRepository).findByProductId(productId);
        verify(stockRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe lanzar InvalidInventoryMovementTypeException si el tipo de movimiento no es válido")
    void shouldThrowExceptionWhenMovementTypeIsInvalid() {
        // Arrange
        String productId = "prod-123";
        RegisterInventoryMovementCommand command = new RegisterInventoryMovementCommand(
                productId, "UNKNOWN_TYPE", 10, "Invalid", "user-1"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(Product.builder().id(productId).build()));
        
        Stock stock = Stock.builder().productId(productId).quantity(10).minStock(2).build();
        when(stockRepository.findByProductId(productId)).thenReturn(Optional.of(stock));

        // Act & Assert
        assertThatThrownBy(() -> registerInventoryMovementService.execute(command))
                .isInstanceOf(InvalidInventoryMovementTypeException.class);

        // Verify interactions
        verify(stockRepository, never()).save(any());
        verify(movementRepository, never()).save(any());
    }
}