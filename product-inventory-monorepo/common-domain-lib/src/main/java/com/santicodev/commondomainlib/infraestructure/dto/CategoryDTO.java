package com.santicodev.commondomainlib.infraestructure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Ver exlipcacion y anotaciones en el notion
 * - Proyecto: Gestor Inventario de Productos
 */
public record CategoryDTO(
        Long id,
        @NotBlank(message = "El nombre de la categoría no puede estar vacío")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String name,
        @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
        String description
) {
}
