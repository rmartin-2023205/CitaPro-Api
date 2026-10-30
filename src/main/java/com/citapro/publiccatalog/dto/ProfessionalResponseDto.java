package com.citapro.publiccatalog.dto;

import com.citapro.professionals.model.Professional;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("null")
public record ProfessionalResponseDto(
    UUID id,
    UUID businessId,
    String displayName,
    String bio,
    List<ServiceResponseDto> services
) {
    public static ProfessionalResponseDto fromEntity(Professional professional) {
        List<ServiceResponseDto> serviceDtos = professional.getServices().stream()
            .filter(com.citapro.services.model.ServiceEntity::isActive)
            .map(ServiceResponseDto::fromEntity)
            .toList();

        return new ProfessionalResponseDto(
            professional.getId(),
            professional.getBusiness().getId(),
            professional.getDisplayName(),
            professional.getBio(),
            serviceDtos
        );
    }
}

