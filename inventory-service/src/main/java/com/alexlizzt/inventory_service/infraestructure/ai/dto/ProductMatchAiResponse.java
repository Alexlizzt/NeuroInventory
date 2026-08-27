package com.alexlizzt.inventory_service.infraestructure.ai.dto;

public record ProductMatchAiResponse(
    String productId,
    Double score,
    String rationale
) {

}
