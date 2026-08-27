package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

public record SemanticSearchProductResponse(
    String id,
    String categoryId,
    String sku,
    String name,
    String description,
    BigDecimal price,
    Boolean active,
    Double score,
    String rationale
) {

}
