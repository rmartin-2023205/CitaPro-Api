package com.citapro.publiccatalog.service;

import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.publiccatalog.dto.BusinessResponseDto;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.publiccatalog.dto.ServiceResponseDto;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublicCatalogServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @InjectMocks
    private PublicCatalogService publicCatalogService;

    private Business mockBusiness;
    private ServiceEntity mockService;
    private Professional mockProfessional;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("Barbería Demo", "barberia-demo", "America/Guatemala", "12345678", "demo@citapro.com", "Zona 1");
        mockBusiness.setId(UUID.randomUUID());

        mockService = new ServiceEntity(mockBusiness, "Corte Clásico", "Corte con tijera", 30, BigDecimal.valueOf(50.00));
        mockService.setId(UUID.randomUUID());

        mockProfessional = new Professional(mockBusiness, "Carlos Barbero", "Experto en cortes clásicos");
        mockProfessional.setId(UUID.randomUUID());
        mockProfessional.setServices(Set.of(mockService));
    }

    @Test
    @DisplayName("Debe retornar la información del negocio activo")
    void getBusiness_Success() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));

        BusinessResponseDto dto = publicCatalogService.getBusiness();

        assertNotNull(dto);
        assertEquals("Barbería Demo", dto.name());
        assertEquals("barberia-demo", dto.slug());
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si no hay negocio activo")
    void getBusiness_NotFound() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> publicCatalogService.getBusiness());
    }

    @Test
    @DisplayName("Debe retornar la lista de servicios activos")
    void getServices_Success() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        when(serviceRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(mockBusiness.getId()))
            .thenReturn(List.of(mockService));

        List<ServiceResponseDto> services = publicCatalogService.getServices();

        assertNotNull(services);
        assertEquals(1, services.size());
        assertEquals("Corte Clásico", services.get(0).name());
        assertEquals(BigDecimal.valueOf(50.00), services.get(0).price());
    }

    @Test
    @DisplayName("Debe retornar los profesionales con sus servicios")
    void getProfessionals_Success() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        when(professionalRepository.findByBusinessIdAndActiveTrueWithServices(mockBusiness.getId()))
            .thenReturn(List.of(mockProfessional));

        List<ProfessionalResponseDto> professionals = publicCatalogService.getProfessionals();

        assertNotNull(professionals);
        assertEquals(1, professionals.size());
        assertEquals("Carlos Barbero", professionals.get(0).displayName());
        assertEquals(1, professionals.get(0).services().size());
    }

    @Test
    @DisplayName("Debe retornar los servicios de un profesional específico")
    void getServicesForProfessional_Success() {
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId()))
            .thenReturn(Optional.of(mockProfessional));

        List<ServiceResponseDto> services = publicCatalogService.getServicesForProfessional(mockProfessional.getId());

        assertNotNull(services);
        assertEquals(1, services.size());
        assertEquals("Corte Clásico", services.get(0).name());
    }
}

