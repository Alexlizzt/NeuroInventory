package com.alexlizzt.inventory_service.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(
    name = "UpdateCategoryRequest",
    description = "Datos necesarios para actualizar una categoría existente"
)
public record UpdateCategoryRequest(

    @Schema(
        description = "Nombre de la categoría",
        example = "Electrónica",
        minLength = 2,
        maxLength = 100
    )
    @Size(
        min = 2,
        max = 100,
        message = "El nombre de la categoría debe tener entre 2 y 100 caracteres"
    )
    String name,

    @Schema(
        description = "Descripción opcional de la categoría",
        example = "Productos electrónicos y dispositivos tecnológicos",
        maxLength = 255
    )
    @Size(
        max = 255,
        message = "La descripción no puede superar los 255 caracteres"
    )
    String description
) {

}
