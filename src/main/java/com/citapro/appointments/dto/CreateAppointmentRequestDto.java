package com.citapro.appointments.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record CreateAppointmentRequestDto(
    @NotNull(message = "El servicio es requerido")
    UUID serviceId,

    @NotNull(message = "El profesional es requerido")
    UUID professionalId,

    @NotNull(message = "La fecha y hora de inicio es requerida")
    Instant startAt,

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    String notes
) {}
