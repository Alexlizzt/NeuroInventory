package com.alexlizzt.inventory_service.infraestructure.integration;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.alexlizzt.inventory_service.application.port.AiServiceClient;
import com.alexlizzt.inventory_service.domain.model.Product;

@Component
public class AiRestClientAdapter implements AiServiceClient {

    private final RestClient restClient;

    public AiRestClientAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${ollama.host}") String host,
            @Value("${ollama.port}") int port
    ) {
        String baseUrl = "http://" + host + ":" + port;
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<Product> searchSemantically(String query, int limit) {
        // RestClient se encarga automáticamente de hacer la petición y convertir el JSON a List<Product>
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/ai/semantic-search")
                        .queryParam("query", query)
                        .queryParam("limit", limit)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Product>>() {});
    }
}
