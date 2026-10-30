package com.citapro.services.dto;

import java.math.BigDecimal;

public record UpdateServiceRequestDto(
    String name,
    String description,
    Integer durationMinutes,
    BigDecimal price,
    Boolean active
) {}

