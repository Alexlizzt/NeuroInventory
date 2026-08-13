package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import org.springframework.stereotype.Service;

import com.alexlizzt.inventory_service.application.port.AiServiceClient;
import com.alexlizzt.inventory_service.domain.model.Product;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchProductsSemanticallyUseCase {
    private final AiServiceClient aiServiceClient;

    public List<Product> execute(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("La consulta de búsqueda semántica no puede estar vacía.");
        }

        // Delegamos la búsqueda inteligente al AI Service (FastAPI)
        // El AI Service se encargará de hablar con Ollama, generar el vector y consultar pgvector.
        int sanitizedLimit = limit <= 0 ? 5 : limit;
        
        return aiServiceClient.searchSemantically(query, sanitizedLimit);
    }
}
