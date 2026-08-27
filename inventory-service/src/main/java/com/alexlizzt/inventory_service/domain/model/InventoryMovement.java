package com.alexlizzt.inventory_service.domain.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

    private String id;
    private String productId;
    private String type;
    private int quantity;
    private String reason;
    private String userId;
    private LocalDateTime createdAt;

}