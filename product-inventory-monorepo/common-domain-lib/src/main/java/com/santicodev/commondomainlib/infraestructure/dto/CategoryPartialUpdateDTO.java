package com.santicodev.commondomainlib.infraestructure.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPartialUpdateDTO {
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String name; // Campo opcional para actualizar

    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String description; // Campo opcional para actualizar
}
