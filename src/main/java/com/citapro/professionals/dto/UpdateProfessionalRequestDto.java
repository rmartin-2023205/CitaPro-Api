package com.citapro.professionals.dto;

import java.util.Set;
import java.util.UUID;

public record UpdateProfessionalRequestDto(
    String displayName,
    String bio,
    Boolean active,
    Set<UUID> serviceIds
) {}

