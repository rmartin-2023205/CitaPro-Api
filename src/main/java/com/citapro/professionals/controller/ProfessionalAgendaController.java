package com.citapro.professionals.controller;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.professionals.service.ProfessionalAgendaService;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/professional")
@PreAuthorize("hasAnyRole('PROFESSIONAL', 'ADMIN')")
@Tag(name = "Agenda Profesional", description = "Endpoints para la agenda y gestión de citas del especialista autenticado")
public class ProfessionalAgendaController {

    private final ProfessionalAgendaService agendaService;

    public ProfessionalAgendaController(ProfessionalAgendaService agendaService) {
        this.agendaService = agendaService;
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del especialista", description = "Retorna el perfil del especialista asociado al usuario autenticado.")
    public ResponseEntity<ProfessionalResponseDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(agendaService.getMyProfile(authentication.getName()));
    }

    @GetMapping("/agenda")
    @Operation(summary = "Consultar agenda del especialista", description = "Retorna las citas asignadas al especialista autenticado en un rango de fechas.")
    public ResponseEntity<List<AppointmentResponseDto>> getAgenda(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            Authentication authentication) {
        return ResponseEntity.ok(agendaService.getAgenda(authentication.getName(), from, to));
    }
}

