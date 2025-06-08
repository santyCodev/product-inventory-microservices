package com.santicodev.commondomainlib.security.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        String username,

        @NotBlank
        @Size(min = 6, max = 40)
        String password

        // Puedes añadir roles aquí si quieres que el registro permita roles específicos,
        // o siemplemente asignar un rol por defecto en el servicio.
        // private Set<String> roles;
) {
}
