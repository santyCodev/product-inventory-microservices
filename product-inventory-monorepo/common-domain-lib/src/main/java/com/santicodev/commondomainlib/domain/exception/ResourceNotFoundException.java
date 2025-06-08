package com.santicodev.commondomainlib.domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 1. Anotación de Spring: Cuando esta excepción es lanzada,
// Spring automáticamente responderá con un código de estado HTTP 404 Not Found.
@ResponseStatus(HttpStatus.NOT_FOUND)
// 2. Extiende RuntimeException: Es una excepción no chequeada, no necesitas declararla en la firma de los métodos.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        // Llama al constructor de la clase padre para pasar el mensaje de error.
        super(message);
    }
}
