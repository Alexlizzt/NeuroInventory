package com.alexlizzt.inventory_service.infraestructure.web.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alexlizzt.inventory_service.application.dto.response.CategoryResponse;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.application.usecase.CreateCategoryUseCase;
import com.alexlizzt.inventory_service.application.usecase.GetCategoriesUseCase;

@WebMvcTest(
    controllers = CategoryController.class,
    properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration"
    }
)
@Import(TestSecurityConfig.class)
class CategoryControllerWebTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateCategoryUseCase createCategoryUseCase;

    @MockitoBean
    private GetCategoriesUseCase listCategoriesUseCase;

    @Test
    void shouldCreateCategoryAsAdmin() throws Exception {

        // Arrange
        CategoryResponse response = new CategoryResponse(
                "cat-123456",
                "Electrónica",
                "Productos electrónicos"
        );

        when(createCategoryUseCase.execute(any()))
                .thenReturn(response);

        String json = """
                {
                    "name": "Electrónica",
                    "description": "Productos electrónicos"
                }
                """;

        // Act + Assert
        mockMvc.perform(
                post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(status().isCreated());
    }

    @Test
    void shouldNotCreateCategoryAsUser() throws Exception {

        String json = """
                {
                    "name": "Electrónica",
                    "description": "Productos electrónicos"
                }
                """;

        mockMvc.perform(
                post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("user")
                                        .roles("USER")
                        )
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsEmpty() throws Exception {

        String json = """
                {
                    "name": "",
                    "description": "Productos electrónicos"
                }
                """;

        mockMvc.perform(
                post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenNameIsTooShort() throws Exception {

        String json = """
                {
                    "name": "A",
                    "description": "Productos electrónicos"
                }
                """;

        mockMvc.perform(
                post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetCategoriesAsUser() throws Exception {

        PageResult<CategoryResponse> pageResult = new PageResult<>(
                List.of(
                        new CategoryResponse(
                                "cat-001",
                                "Electrónica",
                                "Productos electrónicos"
                        ),
                        new CategoryResponse(
                                "cat-002",
                                "Hogar",
                                "Productos para el hogar"
                        )
                ),
                0,
                10,
                2,
                1
        );

        when(listCategoriesUseCase.execute(any()))
                .thenReturn(pageResult);

        mockMvc.perform(
                get("/categories")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("direction", "ASC")
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("user")
                                        .roles("USER")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void shouldGetCategoriesAsAdmin() throws Exception {

        PageResult<CategoryResponse> pageResult = new PageResult<>(
                List.of(),
                0,
                10,
                0,
                0
        );

        when(listCategoriesUseCase.execute(any()))
                .thenReturn(pageResult);

        mockMvc.perform(
                get("/categories")
                        .with(
                                org.springframework.security.test.web.servlet.request
                                        .SecurityMockMvcRequestPostProcessors
                                        .user("admin")
                                        .roles("ADMIN")
                        )
        )
        .andExpect(status().isOk());
    }

    @Test
    void shouldNotGetCategoriesWithoutAuthentication() throws Exception {

        mockMvc.perform(
                get("/categories")
        )
        .andExpect(status().isUnauthorized());
    }
}
