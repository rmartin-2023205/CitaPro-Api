package com.citapro.appointments.dto;

import jakarta.validation.constraints.Size;

public record CancelAppointmentRequestDto(
    @Size(max = 250, message = "El motivo no puede exceder 250 caracteres")
    String reason
) {}
