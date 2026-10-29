package com.citapro.publiccatalog.dto;

import com.citapro.services.model.ServiceEntity;

import java.math.BigDecimal;
import java.util.UUID;

public record ServiceResponseDto(
    UUID id,
    UUID businessId,
    String name,
    String description,
    int durationMinutes,
    BigDecimal price
) {
    public static ServiceResponseDto fromEntity(ServiceEntity service) {
        return new ServiceResponseDto(
            service.getId(),
            service.getBusiness().getId(),
            service.getName(),
            service.getDescription(),
            service.getDurationMinutes(),
            service.getPrice()
        );
    }
}

