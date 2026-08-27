package com.alexlizzt.inventory_service.application.usecase;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alexlizzt.inventory_service.application.dto.response.SemanticSearchProductResponse;
import com.alexlizzt.inventory_service.domain.model.Product;
import com.alexlizzt.inventory_service.domain.model.SemanticMatch;
import com.alexlizzt.inventory_service.domain.port.AiServicePort;
import com.alexlizzt.inventory_service.domain.repository.ProductRepository;

@Service
public class SearchProductsSemanticallyUseCase {
    private final AiServicePort aiServicePort;
    private final ProductRepository productRepository;

    public SearchProductsSemanticallyUseCase(
            AiServicePort aiServicePort,
            ProductRepository productRepository) {
        this.aiServicePort = aiServicePort;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public List<SemanticSearchProductResponse> execute(String query, int limit) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }

        // 1. Obtener coincidencias semánticas (IDs, score, rationale) desde FastAPI a través del Puerto
        List<SemanticMatch> matches = aiServicePort.searchSemantically(query, limit);

        if (matches.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extraer los IDs de los productos encontrados
        List<String> productIds = matches.stream()
                .map(SemanticMatch::getProductId)
                .toList();

        // 3. Consultar en la base de datos relacional la información completa de esos productos
        List<Product> products = productRepository.findAllByIds(productIds);

        Map<String, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 4. Combinar la información respetando el orden de relevancia devuelto por la IA
        return matches.stream()
                .filter(match -> productMap.containsKey(match.getProductId()))
                .map(match -> {
                    Product product = productMap.get(match.getProductId());
                    return new SemanticSearchProductResponse(
                            product.getId(),
                            product.getCategoryId(),
                            product.getSku(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getActive(),
                            match.getScore(),
                            match.getRationale()
                    );
                })
                .toList();
    }
}
