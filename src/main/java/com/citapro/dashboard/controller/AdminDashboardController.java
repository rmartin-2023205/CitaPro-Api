package com.citapro.dashboard.controller;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.availability.dto.AvailabilityBlockResponseDto;
import com.citapro.availability.dto.CreateAvailabilityBlockDto;
import com.citapro.dashboard.dto.DashboardSummaryDto;
import com.citapro.dashboard.service.AdminDashboardService;
import com.citapro.dashboard.service.AdminManagementService;
import com.citapro.professionals.dto.CreateProfessionalRequestDto;
import com.citapro.professionals.dto.UpdateProfessionalRequestDto;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.publiccatalog.dto.ServiceResponseDto;
import com.citapro.services.dto.CreateServiceRequestDto;
import com.citapro.services.dto.UpdateServiceRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administración", description = "Endpoints para el panel administrativo, métricas, CRUD de servicios, profesionales y bloqueos")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;
    private final AdminManagementService managementService;

    public AdminDashboardController(
            AdminDashboardService dashboardService,
            AdminManagementService managementService) {
        this.dashboardService = dashboardService;
        this.managementService = managementService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Obtener resumen del Dashboard", description = "Métricas de citas del día, ingresos estimados y totales.")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(dashboardService.getDashboardSummary());
    }

    @GetMapping("/appointments")
    @Operation(summary = "Listar citas con filtros", description = "Consulta global de citas filtrables por rango de fechas, profesional y estado.")
    public ResponseEntity<List<AppointmentResponseDto>> getAppointments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam(required = false) AppointmentStatus status) {
        return ResponseEntity.ok(managementService.getAdminAppointments(from, to, professionalId, status));
    }

    // --- SERVICIOS ---
    @GetMapping("/services")
    @Operation(summary = "Listar todos los servicios", description = "Retorna el catálogo completo para administración.")
    public ResponseEntity<List<ServiceResponseDto>> getAllServices() {
        return ResponseEntity.ok(managementService.getAllServices());
    }

    @PostMapping("/services")
    @Operation(summary = "Crear nuevo servicio", description = "Añade un servicio al catálogo.")
    public ResponseEntity<ServiceResponseDto> createService(@Valid @RequestBody CreateServiceRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.createService(dto));
    }

    @PatchMapping("/services/{id}")
    @Operation(summary = "Actualizar servicio", description = "Modifica los datos de un servicio.")
    public ResponseEntity<ServiceResponseDto> updateService(
            @PathVariable UUID id,
            @RequestBody UpdateServiceRequestDto dto) {
        return ResponseEntity.ok(managementService.updateService(id, dto));
    }

    @DeleteMapping("/services/{id}")
    @Operation(summary = "Desactivar servicio", description = "Desactiva lógicamente un servicio para conservar historial.")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        managementService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    // --- PROFESIONALES ---
    @GetMapping("/professionals")
    @Operation(summary = "Listar todos los profesionales", description = "Retorna el personal del negocio.")
    public ResponseEntity<List<ProfessionalResponseDto>> getAllProfessionals() {
        return ResponseEntity.ok(managementService.getAllProfessionals());
    }

    @PostMapping("/professionals")
    @Operation(summary = "Crear nuevo profesional", description = "Registra un especialista y sus servicios.")
    public ResponseEntity<ProfessionalResponseDto> createProfessional(@Valid @RequestBody CreateProfessionalRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.createProfessional(dto));
    }

    @PatchMapping("/professionals/{id}")
    @Operation(summary = "Actualizar profesional", description = "Modifica los datos y servicios de un profesional.")
    public ResponseEntity<ProfessionalResponseDto> updateProfessional(
            @PathVariable UUID id,
            @RequestBody UpdateProfessionalRequestDto dto) {
        return ResponseEntity.ok(managementService.updateProfessional(id, dto));
    }

    // --- BLOQUEOS Y DESCANSOS ---
    @GetMapping("/blocks")
    @Operation(summary = "Listar bloqueos de horario", description = "Retorna los descansos y bloqueos configurados.")
    public ResponseEntity<List<AvailabilityBlockResponseDto>> getAllBlocks() {
        return ResponseEntity.ok(managementService.getAllBlocks());
    }

    @PostMapping("/blocks")
    @Operation(summary = "Crear bloqueo de horario", description = "Bloquea un rango temporal para el negocio o especialista.")
    public ResponseEntity<AvailabilityBlockResponseDto> createBlock(
            @Valid @RequestBody CreateAvailabilityBlockDto dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(managementService.createBlock(dto, authentication.getName()));
    }

    @DeleteMapping("/blocks/{id}")
    @Operation(summary = "Eliminar bloqueo de horario", description = "Libera el rango de horario bloqueado.")
    public ResponseEntity<Void> deleteBlock(@PathVariable UUID id) {
        managementService.deleteBlock(id);
        return ResponseEntity.noContent().build();
    }
}

