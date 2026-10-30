package com.citapro.dashboard.service;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.availability.dto.AvailabilityBlockResponseDto;
import com.citapro.availability.dto.CreateAvailabilityBlockDto;
import com.citapro.availability.model.AvailabilityBlock;
import com.citapro.availability.repository.AvailabilityBlockRepository;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.professionals.dto.CreateProfessionalRequestDto;
import com.citapro.professionals.dto.UpdateProfessionalRequestDto;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.publiccatalog.dto.ServiceResponseDto;
import com.citapro.services.dto.CreateServiceRequestDto;
import com.citapro.services.dto.UpdateServiceRequestDto;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminManagementServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private AvailabilityBlockRepository blockRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminManagementService managementService;

    private Business mockBusiness;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("Barbería El Éxito", "America/Guatemala", "info@elexito.gt", "+502 2222-1111");
        mockBusiness.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Debe listar todos los servicios del negocio")
    void testGetAllServices() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        ServiceEntity s1 = new ServiceEntity(mockBusiness, "Corte Clásico", "Corte con tijera", 30, BigDecimal.valueOf(50.00));
        s1.setId(UUID.randomUUID());

        when(serviceRepository.findByBusinessIdOrderByNameAsc(mockBusiness.getId())).thenReturn(List.of(s1));

        List<ServiceResponseDto> result = managementService.getAllServices();
        assertEquals(1, result.size());
        assertEquals("Corte Clásico", result.get(0).name());
    }

    @Test
    @DisplayName("Debe crear un nuevo servicio correctamente")
    void testCreateService() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        CreateServiceRequestDto dto = new CreateServiceRequestDto("Barba Express", "Perfilado de barba", 20, BigDecimal.valueOf(35.00));

        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(inv -> {
            ServiceEntity entity = inv.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        ServiceResponseDto created = managementService.createService(dto);
        assertNotNull(created);
        assertEquals("Barba Express", created.name());
        assertEquals(20, created.durationMinutes());
    }

    @Test
    @DisplayName("Debe actualizar un servicio existente")
    void testUpdateService() {
        UUID serviceId = UUID.randomUUID();
        ServiceEntity existing = new ServiceEntity(mockBusiness, "Corte", "Desc", 30, BigDecimal.valueOf(50.00));
        existing.setId(serviceId);

        when(serviceRepository.findById(serviceId)).thenReturn(Optional.of(existing));
        when(serviceRepository.save(any(ServiceEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateServiceRequestDto dto = new UpdateServiceRequestDto("Corte Premium", "Nueva desc", 45, BigDecimal.valueOf(75.00), true);
        ServiceResponseDto updated = managementService.updateService(serviceId, dto);

        assertEquals("Corte Premium", updated.name());
        assertEquals(45, updated.durationMinutes());
        assertEquals(BigDecimal.valueOf(75.00), updated.price());
    }

    @Test
    @DisplayName("Debe crear un nuevo profesional con servicios asociados")
    void testCreateProfessional() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        UUID serviceId = UUID.randomUUID();
        ServiceEntity service = new ServiceEntity(mockBusiness, "Corte", "Desc", 30, BigDecimal.valueOf(50.00));
        service.setId(serviceId);

        when(serviceRepository.findAllById(any())).thenReturn(List.of(service));
        when(professionalRepository.save(any(Professional.class))).thenAnswer(inv -> {
            Professional p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        CreateProfessionalRequestDto dto = new CreateProfessionalRequestDto("Mario Bros", "Barbero master", null, Set.of(serviceId));
        ProfessionalResponseDto created = managementService.createProfessional(dto);

        assertNotNull(created);
        assertEquals("Mario Bros", created.displayName());
        assertEquals(1, created.services().size());
    }

    @Test
    @DisplayName("Debe crear un bloqueo de horario exitosamente")
    void testCreateBlockSuccess() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        User adminUser = new User();
        adminUser.setEmail("admin@citapro.com");
        when(userRepository.findByEmailWithRoles("admin@citapro.com")).thenReturn(Optional.of(adminUser));

        Instant start = Instant.now().plus(1, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        CreateAvailabilityBlockDto dto = new CreateAvailabilityBlockDto(null, start, end, "Mantenimiento general");

        when(blockRepository.save(any(AvailabilityBlock.class))).thenAnswer(inv -> {
            AvailabilityBlock b = inv.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        AvailabilityBlockResponseDto block = managementService.createBlock(dto, "admin@citapro.com");
        assertNotNull(block);
        assertEquals("Mantenimiento general", block.reason());
    }

    @Test
    @DisplayName("Debe fallar al crear un bloqueo si fecha fin es anterior a inicio")
    void testCreateBlockInvalidDates() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        when(userRepository.findByEmailWithRoles("admin@citapro.com")).thenReturn(Optional.of(new User()));

        Instant start = Instant.now().plus(2, ChronoUnit.HOURS);
        Instant end = start.minus(1, ChronoUnit.HOURS);
        CreateAvailabilityBlockDto dto = new CreateAvailabilityBlockDto(null, start, end, "Error");

        assertThrows(IllegalArgumentException.class, () -> managementService.createBlock(dto, "admin@citapro.com"));
    }
}
