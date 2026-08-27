package com.alexlizzt.inventory_service.infraestructure.ai.adapter;

import java.util.List;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.alexlizzt.inventory_service.domain.model.SemanticMatch;
import com.alexlizzt.inventory_service.infraestructure.ai.dto.SemanticSearchAiRequest;
import com.alexlizzt.inventory_service.infraestructure.ai.dto.SemanticSearchAiResponse;
@Component
public class AiRestClientAdapter implements com.alexlizzt.inventory_service.domain.port.AiServicePort {

    private static final Logger log = LoggerFactory.getLogger(AiRestClientAdapter.class);
    private final RestClient aiRestClient;

    public AiRestClientAdapter(RestClient aiRestClient) {
            this.aiRestClient = aiRestClient;
    }

    @Override
    public List<SemanticMatch> searchSemantically(String query, int limit) {
            var requestPayload = new SemanticSearchAiRequest(query, limit);

            try {
            var response = aiRestClient.post()
                    .uri("/api/v1/search/semantic")
                    .body(requestPayload)
                    .retrieve()
                    .body(SemanticSearchAiResponse.class);

            if (response == null || response.matches() == null) {
                    return Collections.emptyList();
            }

            return response.matches().stream()
                    .map(match -> new SemanticMatch(
                            match.productId(),
                            match.score(),
                            match.rationale()
                    ))
                    .toList();

            } catch (Exception e) {
            log.error("Error calling FastAPI AI Service for semantic search with query: '{}'", query, e);
            // Fallback elegante: listado vacío en caso de fallo externo
            return Collections.emptyList();
            }
    }
}
