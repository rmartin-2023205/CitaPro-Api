package com.citapro.availability.dto;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotDto(
    UUID professionalId,
    String professionalName,
    Instant startAt,
    Instant endAt,
    String formattedTime,
    boolean available
) {}

