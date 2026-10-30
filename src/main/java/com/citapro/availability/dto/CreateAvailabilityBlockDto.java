package com.citapro.availability.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record CreateAvailabilityBlockDto(
    UUID professionalId,

    @NotNull(message = "La fecha y hora de inicio es requerida")
    Instant startAt,

    @NotNull(message = "La fecha y hora de fin es requerida")
    Instant endAt,

    String reason
) {}
