package com.citapro.appointments.dto;

import com.citapro.appointments.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAppointmentStatusRequestDto(
    @NotNull(message = "El nuevo estado es requerido")
    AppointmentStatus status,

    @Size(max = 250, message = "El motivo no puede exceder 250 caracteres")
    String reason
) {}
