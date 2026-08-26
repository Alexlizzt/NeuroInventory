package com.alexlizzt.inventory_service.infraestructure.persistence.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory_movements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovementEntity {
    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "product_id", length = 36, nullable = false)
    private String productId;

    @Column(nullable = false, length = 30)
    private String type; // IN, OUT, ADJUSTMENT

    @Column(nullable = false)
    private Integer quantity;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
