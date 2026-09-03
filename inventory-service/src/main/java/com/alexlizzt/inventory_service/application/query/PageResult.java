package com.alexlizzt.inventory_service.application.query;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "PageResult",
    description = "Resultado paginado de una consulta"
)
public record PageResult<T>(
    @Schema(
        description = "Elementos correspondientes a la página actual",
        example = "[{\"id\":\"prod-123456\",\"name\":\"Teclado mecánico inalámbrico\"}]"
    )
    List<T> content,

    @Schema(
        description = "Número de la página actual, comenzando desde 0",
        example = "0",
        minimum = "0"
    )
    int pageNumber,

    @Schema(
        description = "Cantidad máxima de elementos por página",
        example = "20",
        minimum = "1"
    )
    int pageSize,

    @Schema(
        description = "Cantidad total de elementos disponibles",
        example = "100",
        minimum = "0"
    )
    long totalElements,

    @Schema(
        description = "Cantidad total de páginas disponibles",
        example = "5",
        minimum = "0"
    )
    int totalPages
) { }
