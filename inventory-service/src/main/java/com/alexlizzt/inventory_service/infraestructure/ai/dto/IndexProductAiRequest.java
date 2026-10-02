package com.alexlizzt.inventory_service.infraestructure.ai.dto;

public record IndexProductAiRequest(
    String productId,
    String name,
    String sku,
    String description
) {

}
