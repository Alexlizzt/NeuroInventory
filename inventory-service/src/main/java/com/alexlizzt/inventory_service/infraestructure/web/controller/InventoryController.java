package com.alexlizzt.inventory_service.infraestructure.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alexlizzt.inventory_service.application.command.RegisterInventoryMovementCommand;
import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.application.usecase.GetInventoryHistoryUseCase;
import com.alexlizzt.inventory_service.application.usecase.GetLowStockUseCase;
import com.alexlizzt.inventory_service.application.usecase.GetStockByProductUseCase;
import com.alexlizzt.inventory_service.application.usecase.RegisterInventoryMovementUseCase;
import com.alexlizzt.inventory_service.infraestructure.web.dto.request.RegisterMovementRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventario y Movimientos", description = "Endpoints para la gestión de existencias de stock, control de saldo y registro de movimientos (Kardex)")
public class InventoryController {

    private final RegisterInventoryMovementUseCase registerMovementUseCase;
    private final GetStockByProductUseCase getStockByProductUseCase;
    private final GetLowStockUseCase getLowStockUseCase;
    private final GetInventoryHistoryUseCase getHistoryUseCase;

    public InventoryController(
            RegisterInventoryMovementUseCase registerMovementUseCase,
            GetStockByProductUseCase getStockByProductUseCase,
            GetLowStockUseCase getLowStockUseCase,
            GetInventoryHistoryUseCase getHistoryUseCase) {
        this.registerMovementUseCase = registerMovementUseCase;
        this.getStockByProductUseCase = getStockByProductUseCase;
        this.getLowStockUseCase = getLowStockUseCase;
        this.getHistoryUseCase = getHistoryUseCase;
    }

    @PostMapping("/movements")
    @Operation(
        summary = "Registrar un movimiento de inventario",
        description = "Aplica una entrada (IN) o salida (OUT) de mercancía, actualizando el saldo de stock disponible e inmutando la transacción en el historial."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Movimiento registrado y stock actualizado con éxito",
            content = @Content(schema = @Schema(implementation = InventoryMovementResponse.class))),
        @ApiResponse(responseCode = "400", description = "Parámetros de entrada inválidos o saldo insuficiente en salidas",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado en el sistema de inventario",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<InventoryMovementResponse> registerMovement(
            @Valid @RequestBody RegisterMovementRequest request) {
        var command = new RegisterInventoryMovementCommand(
            request.productId(),
            request.type(),
            request.quantity(),
            request.reason(),
            request.userId()
        );
        InventoryMovementResponse response = registerMovementUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    
    @GetMapping("/stock/{productId}")
    @Operation(summary = "Obtener el stock de un producto", description = "Consulta la disponibilidad de inventario actual y el indicador de stock bajo de un producto específico.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock recuperado exitosamente",
            content = @Content(schema = @Schema(implementation = StockResponse.class))),
        @ApiResponse(responseCode = "404", description = "Registro de stock no encontrado para el producto",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<StockResponse> getStockByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(getStockByProductUseCase.execute(productId));
    }

    @GetMapping("/stock/low")
    @Operation(summary = "Listar productos con stock bajo", description = "Retorna una página con los registros de inventario cuya cantidad actual es menor o igual al stock mínimo configurado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de productos con bajo nivel de inventario",
            content = @Content(schema = @Schema(implementation = PageResult.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<PageResult<StockResponse>> getLowStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageQuery = new PageQuery(page, size);
        return ResponseEntity.ok(getLowStockUseCase.execute(pageQuery));
    }

    @GetMapping("/movements/history/{productId}")
    @Operation(summary = "Consultar historial de movimientos (Kardex)", description = "Obtiene la lista paginada y ordenada cronológicamente de todas las entradas y salidas de inventario.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de historial de movimientos obtenida correctamente",
            content = @Content(schema = @Schema(implementation = PageResult.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<List<InventoryMovementResponse>> getHistory(@PathVariable String productId) {
        return ResponseEntity.ok(getHistoryUseCase.execute(productId));
    }
}