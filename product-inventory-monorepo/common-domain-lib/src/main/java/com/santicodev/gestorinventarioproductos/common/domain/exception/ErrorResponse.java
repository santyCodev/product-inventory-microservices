package com.santicodev.gestorinventarioproductos.common.domain.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp; // Momento en que ocurrió el error.
    private HttpStatus status;       // Código de estado HTTP (ej. 404, 409, 500).
    private String error;            // Mensaje corto de error (ej. "No encontrado", "Conflicto").
    private String message;          // Mensaje descriptivo para el desarrollador/cliente.
    private String path;             // La URL de la solicitud que causó el error.
}
