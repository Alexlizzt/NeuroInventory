package com.alexlizzt.inventory_service.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "CategoryResponse",
    description = "Información de una categoría de productos"
)
public record CategoryResponse(
    @Schema(
        description = "Identificador único de la categoría",
        example = "cat-123456"
    )
    String id,

    @Schema(
        description = "Nombre de la categoría",
        example = "Electrónica"
    )
    String name,

    @Schema(
        description = "Descripción de la categoría",
        example = "Productos electrónicos y dispositivos tecnológicos"
    )
    String description
) {

}
