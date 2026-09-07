package com.alexlizzt.inventory_service.application.usecase;

import java.util.List;

import com.alexlizzt.inventory_service.application.dto.response.SemanticSearchProductResponse;

@FunctionalInterface 
public interface SearchProductsSemanticallyUseCase {
    
    List<SemanticSearchProductResponse> execute(String query, int limit);
}
