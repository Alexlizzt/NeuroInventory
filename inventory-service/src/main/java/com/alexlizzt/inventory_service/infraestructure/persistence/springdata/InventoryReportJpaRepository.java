package com.alexlizzt.inventory_service.infraestructure.persistence.springdata;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.alexlizzt.inventory_service.infraestructure.persistence.entity.InventoryMovementEntity;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.CategoryStockProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.InventorySummaryProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.MovementDetailProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.MovementPeriodProjection;
import com.alexlizzt.inventory_service.infraestructure.persistence.projection.ProductValuationProjection;

/**
 * Repositorio con las consultas agregadas que alimentan los reportes de inventario.
 *
 * Las consultas están expresadas en SQL nativo para resolver las agregaciones
 * (totales, valoraciones y series temporales) directamente en la base de datos.
 */
public interface InventoryReportJpaRepository extends JpaRepository<InventoryMovementEntity, String> {

    @Query(value = """
            SELECT
                (SELECT COUNT(*) FROM products)::bigint AS "totalProducts",
                (SELECT COUNT(*) FROM products WHERE active = TRUE)::bigint AS "activeProducts",
                (SELECT COUNT(*) FROM categories)::bigint AS "totalCategories",
                (SELECT COALESCE(SUM(quantity), 0) FROM stocks)::bigint AS "totalUnits",
                (SELECT COUNT(*) FROM stocks WHERE quantity <= min_stock)::bigint AS "lowStockProducts",
                (SELECT COUNT(*) FROM stocks WHERE quantity = 0)::bigint AS "outOfStockProducts",
                (SELECT COALESCE(SUM(s.quantity * p.price), 0)
                   FROM stocks s JOIN products p ON p.id = s.product_id)::numeric AS "inventoryValue",
                (SELECT COUNT(*) FROM inventory_movements
                   WHERE type = 'IN' AND created_at BETWEEN :from AND :to)::bigint AS "inboundMovements",
                (SELECT COUNT(*) FROM inventory_movements
                   WHERE type = 'OUT' AND created_at BETWEEN :from AND :to)::bigint AS "outboundMovements",
                (SELECT COALESCE(SUM(quantity), 0) FROM inventory_movements
                   WHERE type = 'IN' AND created_at BETWEEN :from AND :to)::bigint AS "inboundUnits",
                (SELECT COALESCE(SUM(quantity), 0) FROM inventory_movements
                   WHERE type = 'OUT' AND created_at BETWEEN :from AND :to)::bigint AS "outboundUnits"
            """, nativeQuery = true)
    InventorySummaryProjection summarize(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query(value = """
            SELECT
                c.id AS "categoryId",
                c.name AS "categoryName",
                COUNT(p.id)::bigint AS "productCount",
                COALESCE(SUM(s.quantity), 0)::bigint AS "totalUnits",
                COALESCE(SUM(s.quantity * p.price), 0)::numeric AS "totalValue",
                COUNT(*) FILTER (WHERE s.quantity <= s.min_stock)::bigint AS "lowStockCount"
            FROM categories c
            LEFT JOIN products p ON p.category_id = c.id
            LEFT JOIN stocks s ON s.product_id = p.id
            GROUP BY c.id, c.name
            ORDER BY "totalValue" DESC, c.name ASC
            """, nativeQuery = true)
    List<CategoryStockProjection> findStockByCategory();

    @Query(value = """
            SELECT
                to_char(date_trunc(:unit, m.created_at AT TIME ZONE 'UTC'), :pattern) AS "period",
                COALESCE(SUM(CASE WHEN m.type = 'IN' THEN m.quantity ELSE 0 END), 0)::bigint AS "inboundUnits",
                COALESCE(SUM(CASE WHEN m.type = 'OUT' THEN m.quantity ELSE 0 END), 0)::bigint AS "outboundUnits",
                COALESCE(SUM(CASE WHEN m.type = 'ADJUSTMENT' THEN ABS(m.quantity) ELSE 0 END), 0)::bigint AS "adjustmentUnits",
                COUNT(*) FILTER (WHERE m.type = 'IN')::bigint AS "inboundMovements",
                COUNT(*) FILTER (WHERE m.type = 'OUT')::bigint AS "outboundMovements",
                COALESCE(SUM(CASE WHEN m.type = 'IN' THEN m.quantity * p.price ELSE 0 END), 0)::numeric AS "inboundValue",
                COALESCE(SUM(CASE WHEN m.type = 'OUT' THEN m.quantity * p.price ELSE 0 END), 0)::numeric AS "outboundValue"
            FROM inventory_movements m
            JOIN products p ON p.id = m.product_id
            WHERE m.created_at BETWEEN :from AND :to
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<MovementPeriodProjection> findMovementsByPeriod(
            @Param("unit") String unit,
            @Param("pattern") String pattern,
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to);

    @Query(value = """
            SELECT
                p.id AS "productId",
                p.sku AS "sku",
                p.name AS "productName",
                p.category_id AS "categoryId",
                c.name AS "categoryName",
                s.quantity::bigint AS "quantity",
                s.min_stock::bigint AS "minStock",
                p.price AS "unitPrice",
                (s.quantity * p.price)::numeric AS "totalValue",
                (SELECT COUNT(*) FROM inventory_movements m WHERE m.product_id = p.id)::bigint AS "movementCount"
            FROM products p
            JOIN stocks s ON s.product_id = p.id
            JOIN categories c ON c.id = p.category_id
            ORDER BY "totalValue" DESC, p.name ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProductValuationProjection> findTopProductsByValue(@Param("limit") int limit);

    @Query(value = """
            SELECT
                p.id AS "productId",
                p.sku AS "sku",
                p.name AS "productName",
                p.category_id AS "categoryId",
                c.name AS "categoryName",
                s.quantity::bigint AS "quantity",
                s.min_stock::bigint AS "minStock",
                p.price AS "unitPrice",
                (s.quantity * p.price)::numeric AS "totalValue",
                COUNT(m.id)::bigint AS "movementCount"
            FROM inventory_movements m
            JOIN products p ON p.id = m.product_id
            JOIN stocks s ON s.product_id = p.id
            JOIN categories c ON c.id = p.category_id
            WHERE m.created_at BETWEEN :from AND :to
            GROUP BY p.id, p.sku, p.name, p.category_id, c.name, s.quantity, s.min_stock, p.price
            ORDER BY "movementCount" DESC, "totalValue" DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ProductValuationProjection> findTopProductsByMovement(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("limit") int limit);

    @Query(value = """
            SELECT
                p.id AS "productId",
                p.sku AS "sku",
                p.name AS "productName",
                p.category_id AS "categoryId",
                c.name AS "categoryName",
                s.quantity::bigint AS "quantity",
                s.min_stock::bigint AS "minStock",
                p.price AS "unitPrice",
                (s.quantity * p.price)::numeric AS "totalValue",
                (SELECT COUNT(*) FROM inventory_movements m WHERE m.product_id = p.id)::bigint AS "movementCount"
            FROM products p
            JOIN stocks s ON s.product_id = p.id
            JOIN categories c ON c.id = p.category_id
            WHERE s.quantity <= s.min_stock
            ORDER BY (s.min_stock - s.quantity) DESC, p.name ASC
            """, nativeQuery = true)
    List<ProductValuationProjection> findLowStockProducts();

    @Query(value = """
            SELECT
                m.id AS "movementId",
                m.product_id AS "productId",
                p.sku AS "sku",
                p.name AS "productName",
                p.category_id AS "categoryId",
                c.name AS "categoryName",
                m.type AS "type",
                m.quantity::bigint AS "quantity",
                p.price AS "unitPrice",
                (m.quantity * p.price)::numeric AS "totalValue",
                m.reason AS "reason",
                m.user_id AS "userId",
                (m.created_at AT TIME ZONE 'UTC') AS "createdAt"
            FROM inventory_movements m
            JOIN products p ON p.id = m.product_id
            JOIN categories c ON c.id = p.category_id
            WHERE m.created_at BETWEEN :from AND :to
              AND (CAST(:type AS VARCHAR) IS NULL OR m.type = CAST(:type AS VARCHAR))
              AND (CAST(:categoryId AS VARCHAR) IS NULL OR p.category_id = CAST(:categoryId AS VARCHAR))
            ORDER BY m.created_at DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<MovementDetailProjection> findMovements(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("type") String type,
            @Param("categoryId") String categoryId,
            @Param("limit") int limit);
}
