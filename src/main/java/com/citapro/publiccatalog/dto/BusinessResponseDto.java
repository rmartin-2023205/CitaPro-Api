package com.citapro.publiccatalog.dto;

import com.citapro.business.model.Business;

import java.util.UUID;

public record BusinessResponseDto(
    UUID id,
    String name,
    String slug,
    String timezone,
    String phone,
    String email,
    String address
) {
    public static BusinessResponseDto fromEntity(Business business) {
        return new BusinessResponseDto(
            business.getId(),
            business.getName(),
            business.getSlug(),
            business.getTimezone(),
            business.getPhone(),
            business.getEmail(),
            business.getAddress()
        );
    }
}

