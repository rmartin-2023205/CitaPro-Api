package com.citapro.publiccatalog.controller;

import com.citapro.availability.dto.AvailabilityResponseDto;
import com.citapro.availability.service.AvailabilityService;
import com.citapro.publiccatalog.dto.BusinessResponseDto;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.publiccatalog.dto.ServiceResponseDto;
import com.citapro.publiccatalog.service.PublicCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public")
@Tag(name = "Catálogo Público y Disponibilidad", description = "Endpoints para consultar catálogo, servicios y disponibilidad de citas")
public class PublicCatalogController {

    private final PublicCatalogService catalogService;
    private final AvailabilityService availabilityService;

    public PublicCatalogController(PublicCatalogService catalogService, AvailabilityService availabilityService) {
        this.catalogService = catalogService;
        this.availabilityService = availabilityService;
    }

    @GetMapping("/business")
    @Operation(summary = "Obtener información del negocio", description = "Retorna detalles del negocio activo, teléfono, dirección y zona horaria.")
    public ResponseEntity<BusinessResponseDto> getBusiness() {
        return ResponseEntity.ok(catalogService.getBusiness());
    }

    @GetMapping("/services")
    @Operation(summary = "Listar servicios activos", description = "Retorna el catálogo público de servicios ordenados alfabéticamente.")
    public ResponseEntity<List<ServiceResponseDto>> getServices() {
        return ResponseEntity.ok(catalogService.getServices());
    }

    @GetMapping("/professionals")
    @Operation(summary = "Listar profesionales activos", description = "Retorna los profesionales disponibles y los servicios que atiende cada uno.")
    public ResponseEntity<List<ProfessionalResponseDto>> getProfessionals() {
        return ResponseEntity.ok(catalogService.getProfessionals());
    }

    @GetMapping("/professionals/{id}/services")
    @Operation(summary = "Listar servicios de un profesional", description = "Retorna los servicios específicos que puede realizar el profesional indicado.")
    public ResponseEntity<List<ServiceResponseDto>> getServicesForProfessional(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getServicesForProfessional(id));
    }

    @GetMapping("/availability")
    @Operation(summary = "Consultar horarios disponibles", description = "Calcula en el servidor los intervalos libres reales en base a horarios de apertura, descansos, bloqueos y citas existentes.")
    public ResponseEntity<AvailabilityResponseDto> getAvailability(
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID professionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availabilityService.getAvailability(serviceId, professionalId, date));
    }
}
