package com.alexlizzt.inventory_service.infraestructure.ai.dto;

import java.util.List;

public record SemanticSearchAiResponse(
    List<ProductMatchAiResponse> matches,
    String interpretedQuery
) {

}
