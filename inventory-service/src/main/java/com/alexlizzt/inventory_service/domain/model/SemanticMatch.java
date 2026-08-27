package com.alexlizzt.inventory_service.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SemanticMatch {
    private String productId;
    private Double score;
    private String rationale;
}
