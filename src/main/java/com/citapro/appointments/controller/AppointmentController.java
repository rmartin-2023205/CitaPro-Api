package com.citapro.appointments.controller;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.dto.CancelAppointmentRequestDto;
import com.citapro.appointments.dto.CreateAppointmentRequestDto;
import com.citapro.appointments.dto.UpdateAppointmentStatusRequestDto;
import com.citapro.appointments.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@Tag(name = "Citas", description = "Endpoints para creación, consulta, cancelación y gestión de citas")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @Operation(summary = "Crear nueva cita", description = "Crea una cita de forma transaccional congelando el precio y validando disponibilidad.")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody CreateAppointmentRequestDto dto,
            Authentication authentication) {
        AppointmentResponseDto response = appointmentService.createAppointment(dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @Operation(summary = "Consultar mis citas", description = "Retorna el historial y próximas citas del cliente autenticado.")
    public ResponseEntity<List<AppointmentResponseDto>> getMyAppointments(Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getMyAppointments(authentication.getName()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar detalle de una cita", description = "Retorna la información detallada de una cita específica si el usuario tiene permisos.")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(
            @PathVariable UUID id,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id, authentication.getName()));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una cita", description = "Cancela una cita conforme a la política del negocio (mínimo 2 horas antes para clientes).")
    public ResponseEntity<AppointmentResponseDto> cancelAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelAppointmentRequestDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, dto, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de cita", description = "Permite a administradores y especialistas cambiar el estado de la cita.")
    public ResponseEntity<AppointmentResponseDto> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAppointmentStatusRequestDto dto,
            Authentication authentication) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, dto, authentication.getName()));
    }
}
