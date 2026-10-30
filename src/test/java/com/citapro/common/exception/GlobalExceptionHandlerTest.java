package com.citapro.common.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Debe transformar ResourceNotFoundException en ProblemDetail 404")
    void handleResourceNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Servicio no encontrado");
        ProblemDetail problem = exceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND.value(), problem.getStatus());
        assertEquals("Recurso no encontrado", problem.getTitle());
        assertEquals("Servicio no encontrado", problem.getDetail());
        assertEquals(URI.create("https://citapro.com/errors/not-found"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar ConflictException en ProblemDetail 409")
    void handleConflict() {
        ConflictException ex = new ConflictException("El horario ya está ocupado");
        ProblemDetail problem = exceptionHandler.handleConflict(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Conflicto de horario o estado", problem.getTitle());
        assertEquals("El horario ya está ocupado", problem.getDetail());
        assertEquals(URI.create("https://citapro.com/errors/conflict"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar ForbiddenException en ProblemDetail 403")
    void handleForbidden() {
        ForbiddenException ex = new ForbiddenException("No tiene permisos para cancelar esta cita");
        ProblemDetail problem = exceptionHandler.handleForbidden(ex);

        assertEquals(HttpStatus.FORBIDDEN.value(), problem.getStatus());
        assertEquals("Acceso no autorizado", problem.getTitle());
        assertEquals("No tiene permisos para cancelar esta cita", problem.getDetail());
        assertEquals(URI.create("https://citapro.com/errors/forbidden"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar IllegalArgumentException en ProblemDetail 400")
    void handleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Rango de fechas inválido");
        ProblemDetail problem = exceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST.value(), problem.getStatus());
        assertEquals("Solicitud inválida", problem.getTitle());
        assertEquals("Rango de fechas inválido", problem.getDetail());
        assertEquals(URI.create("https://citapro.com/errors/bad-request"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar colisión btree_gist no_overlapping_appointments en ProblemDetail 409 de concurrencia")
    void handleDataIntegrityViolationConcurrency() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
            "ERROR: conflicting key value violates exclusion constraint \"no_overlapping_appointments\""
        );
        ProblemDetail problem = exceptionHandler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Colisión de cita simultánea", problem.getTitle());
        assertNotNull(problem.getDetail());
        assertEquals(URI.create("https://citapro.com/errors/concurrency-conflict"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar otra DataIntegrityViolationException en ProblemDetail 409 de integridad")
    void handleGenericDataIntegrityViolation() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key value violates unique constraint");
        ProblemDetail problem = exceptionHandler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT.value(), problem.getStatus());
        assertEquals("Error de integridad", problem.getTitle());
        assertEquals(URI.create("https://citapro.com/errors/integrity-violation"), problem.getType());
    }

    @Test
    @DisplayName("Debe transformar excepción inesperada en ProblemDetail 500 sin exponer detalles sensibles")
    void handleGenericException() {
        RuntimeException ex = new RuntimeException("NullPointerException en capa interna");
        ProblemDetail problem = exceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), problem.getStatus());
        assertEquals("Error interno del servidor", problem.getTitle());
        assertEquals(URI.create("https://citapro.com/errors/internal"), problem.getType());
    }
}

