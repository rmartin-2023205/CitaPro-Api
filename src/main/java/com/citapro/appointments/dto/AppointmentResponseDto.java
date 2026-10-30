package com.citapro.appointments.dto;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponseDto(
    UUID id,
    UUID businessId,
    UUID clientId,
    String clientName,
    String clientEmail,
    String clientPhone,
    UUID professionalId,
    String professionalName,
    UUID serviceId,
    String serviceName,
    int durationMinutes,
    Instant startAt,
    Instant endAt,
    AppointmentStatus status,
    BigDecimal priceAtBooking,
    String notes,
    Instant createdAt,
    Instant cancelledAt,
    boolean canCancel
) {
    public static AppointmentResponseDto fromEntity(Appointment appointment) {
        // Política de cancelación del MVP: solo se puede cancelar si faltan más de 2 horas y está activa (CONFIRMED o PENDING)
        boolean withinCancelWindow = appointment.getStartAt().isAfter(Instant.now().plus(Duration.ofHours(2)));
        boolean canCancel = withinCancelWindow && (appointment.getStatus() == AppointmentStatus.CONFIRMED || appointment.getStatus() == AppointmentStatus.PENDING);

        return new AppointmentResponseDto(
            appointment.getId(),
            appointment.getBusiness().getId(),
            appointment.getClient().getId(),
            appointment.getClient().getFullName(),
            appointment.getClient().getEmail(),
            appointment.getClient().getPhone(),
            appointment.getProfessional().getId(),
            appointment.getProfessional().getDisplayName(),
            appointment.getService().getId(),
            appointment.getService().getName(),
            appointment.getService().getDurationMinutes(),
            appointment.getStartAt(),
            appointment.getEndAt(),
            appointment.getStatus(),
            appointment.getPriceAtBooking(),
            appointment.getNotes(),
            appointment.getCreatedAt(),
            appointment.getCancelledAt(),
            canCancel
        );
    }
}
