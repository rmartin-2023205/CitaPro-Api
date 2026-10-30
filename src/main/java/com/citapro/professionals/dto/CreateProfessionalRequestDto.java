package com.citapro.professionals.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

public record CreateProfessionalRequestDto(
    @NotBlank(message = "El nombre del profesional es requerido")
    String displayName,

    String bio,
    UUID userId,
    Set<UUID> serviceIds
) {}

