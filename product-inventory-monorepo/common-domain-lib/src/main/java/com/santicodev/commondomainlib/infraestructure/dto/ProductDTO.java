package com.santicodev.commondomainlib.infraestructure.dto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Ver exlipcacion y anotaciones en el notion
 * - Proyecto: Gestor Inventario de Productos
 */
public record ProductDTO(
        Long id,

        @NotBlank(message = "El nombre del producto no puede estar vacío")
        @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
        String name,

        @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
        String description,

        @NotNull(message = "El precio no puede ser nulo")
        @Min(value = 0, message = "El precio no puede ser negativo")
        BigDecimal price,

        @NotNull(message = "El stock no puede ser nulo")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock,

        @NotNull(message = "La categoría no puede ser nula")
        Long categoryId
) {
}
