package com.citapro.validation;

import com.citapro.appointments.dto.CreateAppointmentRequestDto;
import com.citapro.auth.dto.RegisterRequestDto;
import com.citapro.availability.dto.CreateAvailabilityBlockDto;
import com.citapro.services.dto.CreateServiceRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DtoValidationAndConcurrencyTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    @DisplayName("RegisterRequestDto válido no debe producir errores de validación")
    void validRegisterRequest() {
        RegisterRequestDto dto = new RegisterRequestDto(
            "Ana López",
            "ana@citapro.com",
            "secreto123",
            "5555-1234"
        );
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RegisterRequestDto inválido debe fallar con correo mal formado y contraseña corta")
    void invalidRegisterRequest() {
        RegisterRequestDto dto = new RegisterRequestDto(
            "A",
            "correo-invalido",
            "123",
            "1234567890123456789012345"
        );
        Set<ConstraintViolation<RegisterRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3);
    }

    @Test
    @DisplayName("CreateAppointmentRequestDto debe rechazar campos nulos y notas mayores a 500 caracteres")
    void invalidCreateAppointmentRequest() {
        String longNotes = "x".repeat(505);
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(null, null, null, longNotes);
        Set<ConstraintViolation<CreateAppointmentRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 4);
    }

    @Test
    @DisplayName("CreateServiceRequestDto debe rechazar duración menor a 5 minutos y precio negativo")
    void invalidCreateServiceRequest() {
        CreateServiceRequestDto dto = new CreateServiceRequestDto(
            "",
            "Descripción",
            3,
            new BigDecimal("-10.00")
        );
        Set<ConstraintViolation<CreateServiceRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.size() >= 3);
    }

    @Test
    @DisplayName("CreateAvailabilityBlockDto válido con rango completo no debe producir violaciones")
    void validAvailabilityBlockRequest() {
        CreateAvailabilityBlockDto dto = new CreateAvailabilityBlockDto(
            UUID.randomUUID(),
            Instant.parse("2026-11-10T14:00:00Z"),
            Instant.parse("2026-11-10T16:00:00Z"),
            "Capacitación"
        );
        Set<ConstraintViolation<CreateAvailabilityBlockDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }
}

