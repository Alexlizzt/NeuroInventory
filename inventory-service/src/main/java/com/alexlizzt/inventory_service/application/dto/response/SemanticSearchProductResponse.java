package com.alexlizzt.inventory_service.application.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "SemanticSearchProductResponse",
    description = "Resultado de un producto obtenido mediante búsqueda semántica"
)
public record SemanticSearchProductResponse(
    @Schema(
        description = "Identificador único del producto",
        example = "prod-123456"
    )
    String id,

    @Schema(
        description = "Identificador único de la categoría a la que pertenece el producto",
        example = "cat-123456"
    )
    String categoryId,

    @Schema(
        description = "Código único utilizado para identificar el producto en el inventario",
        example = "SKU-00123"
    )
    String sku,

    @Schema(
        description = "Nombre del producto",
        example = "Teclado mecánico inalámbrico"
    )
    String name,

    @Schema(
        description = "Descripción del producto",
        example = "Teclado mecánico con iluminación RGB y conexión inalámbrica"
    )
    String description,

    @Schema(
        description = "Precio de venta del producto",
        example = "149.99"
    )
    BigDecimal price,

    @Schema(
        description = "Indica si el producto se encuentra activo",
        example = "true"
    )
    Boolean active,

    @Schema(
        description = "Puntuación de relevancia del producto respecto a la consulta realizada. Un valor mayor indica una mayor similitud o relevancia.",
        example = "0.9235",
        minimum = "0.0"
    )
    Double score,

    @Schema(
        description = "Explicación de por qué el producto fue considerado relevante para la consulta",
        example = "El producto coincide con las características de conectividad inalámbrica y teclado mecánico solicitadas."
    )
    String rationale
) {

}
