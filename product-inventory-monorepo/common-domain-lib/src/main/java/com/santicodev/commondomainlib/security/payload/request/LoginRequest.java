package com.santicodev.commondomainlib.security.payload.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        // Asegura que el campo no esté vacío o sea solo espacios en blanco
        @NotBlank
        String username,

        @NotBlank
        String password
) {
}
