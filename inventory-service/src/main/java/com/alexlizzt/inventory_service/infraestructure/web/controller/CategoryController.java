package com.alexlizzt.inventory_service.infraestructure.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alexlizzt.inventory_service.application.usecase.CreateCategoryUseCase;
import com.alexlizzt.inventory_service.application.usecase.GetCategoriesUseCase;
import com.alexlizzt.inventory_service.domain.service.FindCategoryService;
import com.alexlizzt.inventory_service.domain.service.GetCategoriesService;
import com.alexlizzt.inventory_service.domain.service.UpdateCategoryService;
import com.alexlizzt.inventory_service.application.command.CreateCategoryCommand;
import com.alexlizzt.inventory_service.application.command.UpdateCategoryCommand;
import com.alexlizzt.inventory_service.application.dto.request.CreateCategoryRequest;
import com.alexlizzt.inventory_service.application.dto.request.UpdateCategoryRequest;
import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categorías", description = "Endpoints para la gestión y consulta de categorías de productos")
public class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final GetCategoriesService getCategoriesService;
    private final FindCategoryService findCategoryService;
    private final UpdateCategoryService updateCategoryService;

    public CategoryController(
            CreateCategoryUseCase createCategoryUseCase,
            GetCategoriesService getCategoriesService,
            FindCategoryService findCategoryService,
            UpdateCategoryService updateCategoryService) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.getCategoriesService = getCategoriesService;
        this.findCategoryService = findCategoryService;
        this.updateCategoryService = updateCategoryService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Crear una nueva categoría", description = "Registra una nueva categoría en el sistema asegurando unicidad en el nombre.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o la categoría ya existe",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        var command = new CreateCategoryCommand(request.name(), request.description());
        CategoryResponse response = createCategoryUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(summary = "Obtener categorías paginadas", description = "Recupera una lista paginada de categorías registradas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado de categorías obtenido correctamente",
            content = @Content(schema = @Schema(implementation = PageResult.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<PageResult<CategoryResponse>> getCategories(
            @Parameter(description = "Número de página (inicia en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String direction) {
        
        var query = new PageQuery(page, size, sortBy, direction);
        PageResult<CategoryResponse> response = getCategoriesService.execute(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    @Operation(
        summary = "Obtener una categoría por ID",
        description = "Obtiene la información de una categoría a partir de su identificador."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría encontrada exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content
        )
    })
    public ResponseEntity<CategoryResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(findCategoryService.execute(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
        summary = "Actualizar una categoría",
        description = "Actualiza el nombre y descripción de una categoría existente."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría actualizada exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryResponse.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content
        )
    })
    public ResponseEntity<CategoryResponse> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        var command = new UpdateCategoryCommand(
            id,
            request.name(),
            request.description()
        );

        CategoryResponse response = updateCategoryService.execute(command);

        return ResponseEntity.ok(response);
    }

}
