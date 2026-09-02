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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
@Tag(name = "Productos", description = "Endpoints para el catálogo de productos y búsqueda semántica respaldada por IA")
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
    @Operation(summary = "Crear un nuevo producto", description = "Crea un producto en el catálogo e inicializa su registro de stock correspondiente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
            content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Petición inválida, SKU duplicado o categoría inexistente",
            content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
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
    @Operation( summary = "Obtener un producto por ID", description = "Obtiene la información de un producto a partir de su identificador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado exitosamente",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado",
        content = @Content),
        @ApiResponse(responseCode = "400", description = "El ID proporcionado no es válido",
        content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
        content = @Content
        )
    })
    public ResponseEntity<ProductResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(findProductUseCase.execute(id));
    }

    @GetMapping
    @Operation(summary = "Listar productos paginados", description = "Retorna una página de productos ordenados según los parámetros provistos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista paginada de productos",
            content = @Content(schema = @Schema(implementation = PageResult.class))),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor",
            content = @Content)
    })
    public ResponseEntity<PageResult<ProductResponse>> getProducts(
            @Parameter(description = "Número de página (inicia en 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de la página", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo por el cual ordenar", example = "name")
            @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Dirección de ordenamiento (ASC o DESC)", example = "ASC")
            @RequestParam(defaultValue = "ASC") String direction) {
        
        var query = new PageQuery(page, size, sortBy, direction);
        PageResult<ProductResponse> response = listProductsUseCase.execute(query);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/search/semantic")
    @Operation(
        summary = "Búsqueda semántica de productos vía IA",
        description = "Consulta la API externa de FastAPI (búsqueda vectorial) para recuperar coincidencias conceptuales y enriquecer los datos desde la BD relacional."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coincidencias semánticas encontradas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = SemanticSearchProductResponse.class)))),
        @ApiResponse(responseCode = "400", description = "Parámetros de consulta vacíos o inválidos",
            content = @Content),
        @ApiResponse(responseCode = "503", description = "Servicio externo de IA (FastAPI) no disponible",
            content = @Content)
    })
    public ResponseEntity<List<SemanticSearchProductResponse>> searchSemantically(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(searchSemanticallyUseCase.execute(query, limit));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del catálogo dado su ID de recurso.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteProductUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
