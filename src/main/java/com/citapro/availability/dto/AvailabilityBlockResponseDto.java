package com.citapro.availability.dto;

import com.citapro.availability.model.AvailabilityBlock;
import java.time.Instant;
import java.util.UUID;

public record AvailabilityBlockResponseDto(
    UUID id,
    UUID businessId,
    UUID professionalId,
    String professionalName,
    Instant startAt,
    Instant endAt,
    String reason,
    Instant createdAt
) {
    public static AvailabilityBlockResponseDto fromEntity(AvailabilityBlock block) {
        return new AvailabilityBlockResponseDto(
            block.getId(),
            block.getBusiness().getId(),
            block.getProfessional() != null ? block.getProfessional().getId() : null,
            block.getProfessional() != null ? block.getProfessional().getDisplayName() : "Todo el Negocio",
            block.getStartAt(),
            block.getEndAt(),
            block.getReason(),
            block.getCreatedAt()
        );
    }
}
