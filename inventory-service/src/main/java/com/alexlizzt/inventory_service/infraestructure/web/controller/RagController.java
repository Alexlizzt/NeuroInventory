package com.alexlizzt.inventory_service.infraestructure.web.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;

@RestController
@RequestMapping("/ai/rag")
@Tag(name = "Asistente IA - RAG", description = "Endpoints para ingesta de manuales y consultas conversacionales")
public class RagController {

    private final RestClient aiRestClient;

    // Inyectar la instancia Bean ya configurada con timeouts y baseUrl
    public RagController(RestClient aiRestClient) {
        this.aiRestClient = aiRestClient;
    }

    public record RagRequest(
            @JsonProperty("question") String question,
            @JsonProperty("product_id") String productId
    ) {}

    public record RagResponse(
            @JsonProperty("answer") String answer,
            @JsonProperty("sources") List<String> sources
    ) {}

    @PostMapping("/query")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<RagResponse> queryRag(@RequestBody RagRequest request) {
        RagResponse response = aiRestClient.post()
                .uri("/rag/query")
                .body(request)
                .retrieve()
                .body(RagResponse.class);

        return ResponseEntity.ok(response);
    }
}