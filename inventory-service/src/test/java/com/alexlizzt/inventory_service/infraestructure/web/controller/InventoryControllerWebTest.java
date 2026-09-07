package com.alexlizzt.inventory_service.infraestructure.web.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.alexlizzt.inventory_service.application.dto.response.InventoryMovementResponse;
import com.alexlizzt.inventory_service.application.dto.response.StockResponse;
import com.alexlizzt.inventory_service.application.query.PageResult;
import com.alexlizzt.inventory_service.domain.service.GetInventoryHistoryService;
import com.alexlizzt.inventory_service.domain.service.GetLowStockService;
import com.alexlizzt.inventory_service.domain.service.GetStockByProductService;
import com.alexlizzt.inventory_service.domain.service.RegisterInventoryMovementService;
import com.alexlizzt.inventory_service.infraestructure.web.exception.GlobalExceptionHandler;

@WebMvcTest(
    controllers = InventoryController.class,
    properties = {
        "spring.autoconfigure.exclude=" +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration," +
        "org.springframework.boot.security.oauth2.server.resource.autoconfigure.JwtDecoderAutoConfiguration"
    }
)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class InventoryControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterInventoryMovementService registerMovementService;

    @MockitoBean
    private GetStockByProductService getStockByProductService;

    @MockitoBean
    private GetLowStockService getLowStockService;

    @MockitoBean
    private GetInventoryHistoryService  getHistoryService;


    @Test
    void shouldRegisterInventoryMovement() throws Exception {

        InventoryMovementResponse response =
            new InventoryMovementResponse(
                "MOV-001",
                "PROD-001",
                "IN",
                10,
                "Compra de mercancía",
                "USER-001",
                LocalDateTime.now()
            );

        when(registerMovementService.execute(any()))
            .thenReturn(response);

        String requestJson = """
            {
                "productId": "PROD-001",
                "type": "IN",
                "quantity": 10,
                "reason": "Compra de mercancía",
                "userId": "USER-001"
            }
            """;

        mockMvc.perform(
                post("/inventory/movements")
                    .with(user("admin").roles("ADMIN"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isCreated());

        verify(registerMovementService).execute(any());
    }

    @Test
    void shouldReturnForbiddenWhenRegisteringMovementWithoutAdminRole()
            throws Exception {

        String requestJson = """
            {
                "productId": "PROD-001",
                "type": "IN",
                "quantity": 10,
                "reason": "Compra de mercancía",
                "userId": "USER-001"
            }
            """;

        mockMvc.perform(
                post("/inventory/movements")
                    .with(user("user").roles("USER"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )
            .andExpect(status().isForbidden());
    }


    @Test
    void shouldGetStockByProduct() throws Exception {

        StockResponse response =
            new StockResponse(
                "PROD-001",
                100,
                20,
                false,
                LocalDateTime.now()
            );

        when(getStockByProductService.execute("PROD-001"))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/stock/PROD-001")
                    .with(user("user").roles("USER"))
            )
            .andExpect(status().isOk());

        verify(getStockByProductService)
            .execute("PROD-001");
    }


    @Test
    void shouldAllowAdminToGetStockByProduct() throws Exception {

        StockResponse response =
            new StockResponse(
                "PROD-001",
                100,
                20,
                false,
                LocalDateTime.now()
            );

        when(getStockByProductService.execute("PROD-001"))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/stock/PROD-001")
                    .with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isOk());

        verify(getStockByProductService)
            .execute("PROD-001");
    }


    @Test
    void shouldReturnForbiddenWhenGettingStockWithoutRequiredRole()
            throws Exception {

        mockMvc.perform(
                get("/inventory/stock/PROD-001")
                    .with(user("guest").roles("GUEST"))
            )
            .andExpect(status().isForbidden());
    }


    @Test
    void shouldGetLowStockUsingDefaultPagination()
            throws Exception {

        PageResult<StockResponse> response =
            new PageResult<>(
                List.of(),
                0,
                10,
                0,
                0
            );

        when(getLowStockService.execute(any()))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/stock/low")
                    .with(user("user").roles("USER"))
            )
            .andExpect(status().isOk());

        verify(getLowStockService).execute(any());
    }


    @Test
    void shouldGetLowStockUsingCustomPagination()
            throws Exception {

        PageResult<StockResponse> response =
            new PageResult<>(
                List.of(),
                2,
                20,
                0,
                0
            );

        when(getLowStockService.execute(any()))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/stock/low")
                    .with(user("user").roles("USER"))
                    .param("page", "2")
                    .param("size", "20")
            )
            .andExpect(status().isOk());

        verify(getLowStockService).execute(any());
    }


    @Test
    void shouldReturnForbiddenWhenGettingLowStockWithoutRequiredRole()
            throws Exception {

        mockMvc.perform(
                get("/inventory/stock/low")
                    .with(user("guest").roles("GUEST"))
            )
            .andExpect(status().isForbidden());
    }


    @Test
    void shouldGetInventoryHistory() throws Exception {

        List<InventoryMovementResponse> response =
            List.of(
                new InventoryMovementResponse(
                    "MOV-001",
                    "PROD-001",
                    "IN",
                    10,
                    "Test",
                    "USER-001",
                    LocalDateTime.now()
                )
            );

        when(getHistoryService.execute("PROD-001"))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/movements/history/PROD-001")
                    .with(user("user").roles("USER"))
            )
            .andExpect(status().isOk());

        verify(getHistoryService)
            .execute("PROD-001");
    }


    @Test
    void shouldAllowAdminToGetInventoryHistory()
            throws Exception {

        List<InventoryMovementResponse> response =
            List.of(
                new InventoryMovementResponse(
                    "MOV-001",
                    "PROD-001",
                    "IN",
                    10,
                    "Test",
                    "USER-001",
                    LocalDateTime.now()
                )
            );

        when(getHistoryService.execute("PROD-001"))
            .thenReturn(response);

        mockMvc.perform(
                get("/inventory/movements/history/PROD-001")
                    .with(user("admin").roles("ADMIN"))
            )
            .andExpect(status().isOk());

        verify(getHistoryService)
            .execute("PROD-001");
    }

    
    @Test
    void shouldReturnForbiddenWhenGettingHistoryWithoutRequiredRole()
            throws Exception {

        mockMvc.perform(
                get("/inventory/movements/history/PROD-001")
                    .with(user("guest").roles("GUEST"))
            )
            .andExpect(status().isForbidden());
    }
}