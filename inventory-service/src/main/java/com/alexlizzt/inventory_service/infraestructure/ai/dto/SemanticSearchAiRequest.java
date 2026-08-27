package com.alexlizzt.inventory_service.infraestructure.ai.dto;

public record SemanticSearchAiRequest(
    String query,
    Integer limit) {

}
