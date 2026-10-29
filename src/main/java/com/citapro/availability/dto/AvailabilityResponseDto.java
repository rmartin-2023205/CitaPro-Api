package com.citapro.availability.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponseDto(
    LocalDate date,
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    String timezone,
    List<TimeSlotDto> slots
) {}

