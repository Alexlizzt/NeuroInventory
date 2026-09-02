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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/inventory")
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
    public ResponseEntity<StockResponse> getStockByProduct(@PathVariable String productId) {
        return ResponseEntity.ok(getStockByProductUseCase.execute(productId));
    }

    @GetMapping("/stock/low")
    public ResponseEntity<PageResult<StockResponse>> getLowStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageQuery = new PageQuery(page, size);
        return ResponseEntity.ok(getLowStockUseCase.execute(pageQuery));
    }

    @GetMapping("/movements/history/{productId}")
    public ResponseEntity<List<InventoryMovementResponse>> getHistory(@PathVariable String productId) {
        return ResponseEntity.ok(getHistoryUseCase.execute(productId));
    }
}