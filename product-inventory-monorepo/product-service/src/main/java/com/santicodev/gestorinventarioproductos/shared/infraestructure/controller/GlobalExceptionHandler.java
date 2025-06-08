package com.santicodev.gestorinventarioproductos.shared.infraestructure.controller;

import com.santicodev.gestorinventarioproductos.common.domain.exception.DuplicateResourceException;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ErrorResponse;
import com.santicodev.gestorinventarioproductos.common.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {
    // Mapea este método para manejar ResourceNotFoundException.
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND, // Código de estado HTTP 404
                "No encontrado",
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Mapea para DuplicateResourceException.
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT, // Código de estado HTTP 409
                "Conflicto",
                ex.getMessage(),
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    // Mapea para MethodArgumentNotValidException (cuando @Valid falla).
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage()) // "campo: mensaje de error"
                .collect(Collectors.joining(", ")); // Une los mensajes con comas.

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST, // Código de estado HTTP 400
                "Error de Validación",
                errorMessage, // Muestra los mensajes de los errores de validación.
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // 9. Manejador genérico para cualquier otra excepción no controlada explícitamente.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR, // Código de estado HTTP 500
                "Error Interno del Servidor",
                "Ha ocurrido un error inesperado: " + ex.getMessage(), // Mensaje genérico y el mensaje de la excepción original.
                ((ServletWebRequest) request).getRequest().getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Manejador específico para AccessDeniedException (para el 403)
    @ExceptionHandler(AccessDeniedException.class) // O AuthorizationDeniedException.class si el log lo indica
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage() != null ? ex.getMessage() : "Access Denied. You do not have the necessary permissions.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // Si usas Spring Security 6+ y el log indica AuthorizationDeniedException:
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN,
                "Forbidden",
                ex.getMessage() != null ? ex.getMessage() : "Access Denied. You do not have the necessary permissions.",
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }
}
