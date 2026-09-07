package com.alexlizzt.inventory_service.infraestructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.alexlizzt.inventory_service.application.dto.response.ProductResponse;
import com.alexlizzt.inventory_service.application.dto.response.SemanticSearchProductResponse;
import com.alexlizzt.inventory_service.application.query.PageQuery;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.exception.CategoryNotFoundException;
import com.alexlizzt.inventory_service.domain.exception.DuplicateSkuException;
import com.alexlizzt.inventory_service.domain.exception.ProductNotFoundException;
import com.alexlizzt.inventory_service.domain.service.CreateProductService;
import com.alexlizzt.inventory_service.domain.service.DeleteProductService;
import com.alexlizzt.inventory_service.domain.service.FindProductService;
import com.alexlizzt.inventory_service.domain.service.ListProductService;
import com.alexlizzt.inventory_service.domain.service.SearchProductsSemanticallyService;
import com.alexlizzt.inventory_service.domain.service.UpdateProductService;
import com.alexlizzt.inventory_service.infraestructure.web.exception.GlobalExceptionHandler;

@WebMvcTest(
	controllers = ProductController.class,
	properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration"
    }
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "app.problem.base-uri=https://api.example.com/problems/"
})
class ProductControllerWebTest {
	
	@Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductService createProductService;

    @MockitoBean
    private FindProductService findProductService;

    @MockitoBean
    private ListProductService listProductService;

    @MockitoBean
    private UpdateProductService updateProductService;

    @MockitoBean
    private DeleteProductService deleteProductService;

    @MockitoBean
    private SearchProductsSemanticallyService searchSemanticallyService;


	 @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldCreateProduct() throws Exception {

        ProductResponse response = productResponse();

        when(createProductService.execute(any()))
                .thenReturn(response);

        String request = """
                {
                    "categoryId": "cat-123",
                    "sku": "SKU-001",
                    "name": "Teclado mecánico",
                    "description": "Teclado RGB inalámbrico",
                    "price": 149.99,
                    "initialStock": 20,
                    "minStock": 5
                }
                """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("prod-123"))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Teclado mecánico"))
                .andExpect(jsonPath("$.price").value(149.99))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.categoryId").value("cat-123"));

        verify(createProductService).execute(any());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnBadRequestWhenCreateRequestIsInvalid() throws Exception {

        String request = """
                {
                    "categoryId": "",
                    "sku": "",
                    "name": "",
                    "description": "Producto inválido",
                    "price": -10,
                    "initialStock": null,
                    "minStock": null
                }
                """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/validation-error"))
                .andExpect(jsonPath("$.instance").value("/products"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.categoryId").exists())
                .andExpect(jsonPath("$.errors.sku").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.initialStock").exists())
                .andExpect(jsonPath("$.errors.minStock").exists());

        verifyNoInteractions(createProductService);
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnConflictWhenSkuAlreadyExists() throws Exception {

        when(createProductService.execute(any()))
                .thenThrow(new DuplicateSkuException("SKU-001"));

        String request = """
                {
                    "categoryId": "cat-123",
                    "sku": "SKU-001",
                    "name": "Teclado",
                    "description": "Teclado mecánico",
                    "price": 149.99,
                    "initialStock": 20,
                    "minStock": 5
                }
                """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate SKU"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/duplicate-sku"))
                .andExpect(jsonPath("$.instance").value("/products"))
                .andExpect(jsonPath("$.detail").exists());

        verify(createProductService).execute(any());
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnForbiddenWhenUserTriesToCreateProduct() throws Exception {

        String request = """
                {
                    "categoryId": "cat-123",
                    "sku": "SKU-001",
                    "name": "Teclado",
                    "description": "Teclado mecánico",
                    "price": 149.99,
                    "initialStock": 20,
                    "minStock": 5
                }
                """;

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());

        verifyNoInteractions(createProductService);
    }

	@Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldGetProductById() throws Exception {

        when(findProductService.execute("prod-123"))
                .thenReturn(productResponse());

        mockMvc.perform(get("/products/prod-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-123"))
                .andExpect(jsonPath("$.sku").value("SKU-001"))
                .andExpect(jsonPath("$.name").value("Teclado mecánico"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(findProductService).execute("prod-123");
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {

        when(findProductService.execute("prod-999"))
                .thenThrow(new ProductNotFoundException("prod-999"));

        mockMvc.perform(get("/products/prod-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/product-not-found"))
                .andExpect(jsonPath("$.instance").value("/products/prod-999"))
                .andExpect(jsonPath("$.detail").exists());

        verify(findProductService).execute("prod-999");
    }

	@Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldListProductsWithDefaultParameters() throws Exception {

        PageResult<ProductResponse> pageResult =
                new PageResult<>(
                        List.of(productResponse()),
                        0,
                        10,
                        1,
                        1
                );

        when(listProductService.execute(any(PageQuery.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("prod-123"))
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(listProductService).execute(any(PageQuery.class));
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldListProductsWithCustomParameters() throws Exception {

        PageResult<ProductResponse> pageResult =
                new PageResult<>(
                        List.of(productResponse()),
                        2,
                        20,
                        41,
                        3
                );

        when(listProductService.execute(any(PageQuery.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/products")
                .param("page", "2")
                .param("size", "20")
                .param("sortBy", "price")
                .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(20))
                .andExpect(jsonPath("$.totalElements").value(41))
                .andExpect(jsonPath("$.totalPages").value(3));

        verify(listProductService).execute(any(PageQuery.class));
    }


	@Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldSearchProductsSemantically() throws Exception {

        SemanticSearchProductResponse response =
                new SemanticSearchProductResponse(
                        "prod-123",
                        "cat-123",
                        "SKU-001",
                        "Teclado mecánico",
                        "Teclado RGB inalámbrico",
                        new BigDecimal("149.99"),
                        true,
                        0.9235,
                        "Coincide con las características solicitadas"
                );

        when(searchSemanticallyService.execute("teclado inalámbrico", 5))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/products/search/semantic")
                .param("query", "teclado inalámbrico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("prod-123"))
                .andExpect(jsonPath("$[0].name").value("Teclado mecánico"))
                .andExpect(jsonPath("$[0].score").value(0.9235))
                .andExpect(jsonPath("$[0].rationale")
                        .value("Coincide con las características solicitadas"));

        verify(searchSemanticallyService)
                .execute("teclado inalámbrico", 5);
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnEmptyListWhenSemanticSearchFindsNothing() throws Exception {

        when(searchSemanticallyService.execute("xyz", 5))
                .thenReturn(List.of());

        mockMvc.perform(get("/products/search/semantic")
                .param("query", "xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(searchSemanticallyService)
                .execute("xyz", 5);
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldUseCustomSemanticSearchLimit() throws Exception {

        when(searchSemanticallyService.execute("laptop", 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/products/search/semantic")
                .param("query", "laptop")
                .param("limit", "10"))
                .andExpect(status().isOk());

        verify(searchSemanticallyService)
                .execute("laptop", 10);
    }

	@Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldUpdateProduct() throws Exception {

        when(updateProductService.execute(any()))
                .thenReturn(productResponse());

        String request = """
                {
                    "name": "Teclado mecánico Pro",
                    "description": "Nueva descripción",
                    "price": 199.99,
                    "categoryId": "cat-456",
                    "active": true
                }
                """;

        mockMvc.perform(put("/products/prod-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("prod-123"))
                .andExpect(jsonPath("$.name").value("Teclado mecánico"))
                .andExpect(jsonPath("$.price").value(149.99));

        verify(updateProductService).execute(any());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnNotFoundWhenUpdatingNonExistingProduct() throws Exception {

        when(updateProductService.execute(any()))
                .thenThrow(new ProductNotFoundException("prod-999"));

        String request = """
                {
                    "name": "Teclado actualizado",
                    "price": 199.99,
                    "active": true
                }
                """;

        mockMvc.perform(put("/products/prod-999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/product-not-found"))
                .andExpect(jsonPath("$.instance").value("/products/prod-999"));

        verify(updateProductService).execute(any());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnNotFoundWhenUpdatingWithNonExistingCategory() throws Exception {

        when(updateProductService.execute(any()))
                .thenThrow(new CategoryNotFoundException("cat-999"));

        String request = """
                {
                    "name": "Teclado actualizado",
                    "price": 199.99,
                    "categoryId": "cat-999",
                    "active": true
                }
                """;

        mockMvc.perform(put("/products/prod-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Category not found"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/category-not-found"));

        verify(updateProductService).execute(any());
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnBadRequestWhenUpdateRequestIsInvalid() throws Exception {

        String request = """
                {
                    "name": "A",
                    "description": "Descripción",
                    "price": -100,
                    "active": true
                }
                """;

        mockMvc.perform(put("/products/prod-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());

        verifyNoInteractions(updateProductService);
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidUpdateForUser() throws Exception {

        String request = """
                {
                    "name": "Teclado actualizado",
                    "price": 199.99,
                    "active": true
                }
                """;

        mockMvc.perform(put("/products/prod-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isForbidden());

        verifyNoInteractions(updateProductService);
    }

	@Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldDeleteProduct() throws Exception {

        mockMvc.perform(delete("/products/prod-123"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(deleteProductService)
                .execute("prod-123");
    }


    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void shouldReturnNotFoundWhenDeletingNonExistingProduct() throws Exception {

        doThrow(new ProductNotFoundException("prod-999"))
                .when(deleteProductService)
                .execute("prod-999");

        mockMvc.perform(delete("/products/prod-999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Product not found"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/product-not-found"))
                .andExpect(jsonPath("$.instance").value("/products/prod-999"));

        verify(deleteProductService)
                .execute("prod-999");
    }


    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldForbidDeleteForUser() throws Exception {

        mockMvc.perform(delete("/products/prod-123"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(deleteProductService);
    }

	@Test
    @WithMockUser(authorities = "ROLE_USER")
    void shouldReturnInternalServerErrorWhenUnexpectedExceptionOccurs()
            throws Exception {

        when(findProductService.execute("prod-123"))
                .thenThrow(new RuntimeException("Database unavailable"));

        mockMvc.perform(get("/products/prod-123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title")
                        .value("Internal server error"))
                .andExpect(jsonPath("$.type")
                        .value("https://api.example.com/problems/internal-error"))
                .andExpect(jsonPath("$.instance")
                        .value("/products/prod-123"))
                .andExpect(jsonPath("$.detail")
                        .value("An unexpected internal error occurred."));

        verify(findProductService)
                .execute("prod-123");
    }


	private ProductResponse productResponse() {

        return new ProductResponse(
                "prod-123",
                "SKU-001",
                "Teclado mecánico",
                "Teclado RGB inalámbrico",
                new BigDecimal("149.99"),
                true,
                "cat-123",
                LocalDateTime.of(2026, 9, 3, 15, 30),
                LocalDateTime.of(2026, 9, 3, 16, 45)
        );
    }
}
