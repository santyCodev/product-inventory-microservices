package com.santicodev.gestorinventarioproductos.common.infraestructure.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPartialUpdateDTO    {
    @Size(min = 3, max = 200, message = "El nombre debe tener entre 3 y 200 caracteres")
    private String name;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String description;

    @Min(value = 0, message = "El precio no puede ser negativo")
    private BigDecimal price;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private Integer stock;

    private Long categoryId; // Opcional, pero si se envía debe ser válido
}
