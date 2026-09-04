package com.alexlizzt.inventory_service.infraestructure.web.exception;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.DuplicateCategoryNameException;
import com.alexlizzt.inventory_service.domain.exception.DuplicateSkuException;
import com.alexlizzt.inventory_service.domain.exception.InsufficientStockException;
import com.alexlizzt.inventory_service.domain.exception.InvalidInventoryMovementTypeException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.StockNotFoundException;


import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final String baseProblemUri;

    public GlobalExceptionHandler(@Value("${app.problem.base-uri}") String baseProblemUri) {
        // Aseguramos que termine con "/" para concantenar limpiamente el slug del problema
        this.baseProblemUri = baseProblemUri.endsWith("/") ? baseProblemUri : baseProblemUri + "/";
    }

    // Recurso no encontrado (404)
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(baseProblemUri + "product-not-found"));
        problem.setTitle("Product not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setType(URI.create(baseProblemUri + "category-not-found"));
        problem.setTitle("Category not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    // Conflictos de Regla de Negocio (409)
    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(baseProblemUri + "insufficient-stock"));
        problem.setTitle("Insufficient stock");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(DuplicateSkuException.class)
    public ProblemDetail handleDuplicateSku(DuplicateSkuException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setType(URI.create(baseProblemUri + "duplicate-sku"));
        problem.setTitle("Duplicate SKU");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    // Errores de Validación de Entrada (400) - RFC 9457 Extension "errors"
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            "One or more fields contain invalid values."
        );
        problem.setType(URI.create(baseProblemUri + "validation-error"));
        problem.setTitle("Validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));

        Map<String, List<String>> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.computeIfAbsent(error.getField(), key -> new ArrayList<>())
                       .add(error.getDefaultMessage());
        });

        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    @ExceptionHandler(DuplicateCategoryNameException.class)
    public ProblemDetail handleDuplicateCategoryName(
            DuplicateCategoryNameException ex,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setType(URI.create(baseProblemUri + "duplicate-category-name"));
        problem.setTitle("Duplicate category name");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ProblemDetail handleStockNotFound(
            StockNotFoundException ex,
            HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setType(URI.create(baseProblemUri + "stock-not-found"));
        problem.setTitle("Stock not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(InvalidInventoryMovementTypeException.class)
    public ProblemDetail handleInvalidInventoryMovementType(
            InvalidInventoryMovementTypeException ex,
            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setType(URI.create(baseProblemUri + "invalid-inventory-movement-type"));
        problem.setTitle("Invalid inventory movement type");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
public ProblemDetail handleAccessDenied(
        AccessDeniedException ex,
        HttpServletRequest request) {

    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "You do not have permission to access this resource."
    );

    problem.setType(URI.create(baseProblemUri + "access-denied"));
    problem.setTitle("Access denied");
    problem.setInstance(URI.create(request.getRequestURI()));

    return problem;
}


    // Excepciones genéricas no capturadas (500)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected internal error occurred."
        );
        problem.setType(URI.create(baseProblemUri + "internal-error"));
        problem.setTitle("Internal server error");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

}
