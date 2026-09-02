package com.alexlizzt.inventory_service.infraestructure.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alexlizzt.inventory_service.application.command.CreateProductCommand;
import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.dto.response.SemanticSearchProductResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.application.usecase.CreateProductUseCase;
import com.alexlizzt.inventory_service.application.usecase.DeleteProductUseCase;
import com.alexlizzt.inventory_service.application.usecase.FindProductUseCase;
import com.alexlizzt.inventory_service.application.usecase.ListProductsUseCase;
import com.alexlizzt.inventory_service.application.usecase.SearchProductsSemanticallyUseCase;
import com.alexlizzt.inventory_service.infraestructure.web.dto.request.CreateProductRequest;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final CreateProductUseCase createProductUseCase;
    private final FindProductUseCase findProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final SearchProductsSemanticallyUseCase searchSemanticallyUseCase;

    public ProductController(
            CreateProductUseCase createProductUseCase,
            FindProductUseCase findProductUseCase,
            ListProductsUseCase listProductsUseCase,
            DeleteProductUseCase deleteProductUseCase,
            SearchProductsSemanticallyUseCase searchSemanticallyUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.findProductUseCase = findProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.searchSemanticallyUseCase = searchSemanticallyUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        var command = new CreateProductCommand(
            request.categoryId(),
            request.sku(),
            request.name(),
            request.description(),
            request.price(),
            request.initialStock(),
            request.minStock()
        );
        ProductResponse response = createProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(findProductUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<PageResult<ProductResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var pageQuery = new PageQuery(page, size);
        return ResponseEntity.ok(listProductsUseCase.execute(pageQuery));
    }

    @GetMapping("/search/semantic")
    public ResponseEntity<List<SemanticSearchProductResponse>> searchSemantically(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(searchSemanticallyUseCase.execute(query, limit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
