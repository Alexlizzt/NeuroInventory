package com.alexlizzt.inventory_service.domain.port;

import java.util.List;

import com.alexlizzt.inventory_service.domain.model.SemanticMatch;


public interface AiServicePort {
    List<SemanticMatch> searchSemantically(String query, int limit);
}
