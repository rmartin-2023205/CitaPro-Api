package com.citapro.availability.service;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.availability.dto.AvailabilityResponseDto;
import com.citapro.availability.model.AvailabilityBlock;
import com.citapro.availability.model.BusinessHours;
import com.citapro.availability.model.ProfessionalHours;
import com.citapro.availability.repository.AvailabilityBlockRepository;
import com.citapro.availability.repository.BusinessHoursRepository;
import com.citapro.availability.repository.ProfessionalHoursRepository;
import com.citapro.business.model.Business;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import com.citapro.users.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AvailabilityServiceTest {

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    @Mock
    private ProfessionalHoursRepository professionalHoursRepository;

    @Mock
    private AvailabilityBlockRepository blockRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Business mockBusiness;
    private ServiceEntity mockService;
    private Professional mockProfessional;
    private LocalDate futureMonday;
    private ZoneId zoneId;

    @BeforeEach
    void setUp() {
        zoneId = ZoneId.of("America/Guatemala");
        mockBusiness = new Business("Barbería Test", "barberia-test", "America/Guatemala", "12345678", "test@citapro.com", "Zona 1");
        mockBusiness.setId(UUID.randomUUID());

        mockService = new ServiceEntity(mockBusiness, "Corte Clásico", "Corte 30m", 30, BigDecimal.valueOf(50.00));
        mockService.setId(UUID.randomUUID());

        mockProfessional = new Professional(mockBusiness, "Carlos Barbero", "Experto en cortes");
        mockProfessional.setId(UUID.randomUUID());
        mockProfessional.setServices(Set.of(mockService));

        // Encontrar un lunes futuro
        futureMonday = LocalDate.now(zoneId).plusWeeks(2);
        while (futureMonday.getDayOfWeek() != DayOfWeek.MONDAY) {
            futureMonday = futureMonday.plusDays(1);
        }
    }

    @Test
    @DisplayName("Debe rechazar consulta con fecha en el pasado")
    void getAvailability_PastDate_ThrowsException() {
        LocalDate pastDate = LocalDate.now(zoneId).minusDays(1);

        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));

        assertThrows(IllegalArgumentException.class, () ->
            availabilityService.getAvailability(mockService.getId(), mockProfessional.getId(), pastDate)
        );
    }

    @Test
    @DisplayName("Debe calcular slots correctamente para un día laboral sin citas ni bloqueos")
    void getAvailability_Success() {
        BusinessHours bHours = new BusinessHours(mockBusiness, 1, LocalTime.of(8, 0), LocalTime.of(10, 0), false);
        ProfessionalHours pHours = new ProfessionalHours(mockProfessional, 1, LocalTime.of(8, 0), LocalTime.of(10, 0), false);

        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId()))
            .thenReturn(Optional.of(mockProfessional));
        when(businessHoursRepository.findByBusinessIdAndDayOfWeek(mockBusiness.getId(), 1))
            .thenReturn(Optional.of(bHours));
        when(professionalHoursRepository.findByProfessionalIdAndDayOfWeek(mockProfessional.getId(), 1))
            .thenReturn(Optional.of(pHours));
        when(blockRepository.findOverlappingBlocks(any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        AvailabilityResponseDto response = availabilityService.getAvailability(mockService.getId(), mockProfessional.getId(), futureMonday);

        assertNotNull(response);
        assertEquals(futureMonday, response.date());
        // De 08:00 a 10:00 con duración 30m y paso 30m: 08:00-08:30, 08:30-09:00, 09:00-09:30, 09:30-10:00 -> 4 slots
        assertEquals(4, response.slots().size());
        assertEquals("08:00 - 08:30", response.slots().get(0).formattedTime());
        assertEquals("09:30 - 10:00", response.slots().get(3).formattedTime());
    }

    @Test
    @DisplayName("Debe excluir slots que colisionen con un bloqueo de disponibilidad")
    void getAvailability_WithBlock_ExcludesBlockedSlot() {
        BusinessHours bHours = new BusinessHours(mockBusiness, 1, LocalTime.of(8, 0), LocalTime.of(10, 0), false);

        // Bloqueo de 08:30 a 09:00
        Instant blockStart = ZonedDateTime.of(futureMonday, LocalTime.of(8, 30), zoneId).toInstant();
        Instant blockEnd = ZonedDateTime.of(futureMonday, LocalTime.of(9, 0), zoneId).toInstant();
        AvailabilityBlock block = new AvailabilityBlock(mockBusiness, mockProfessional, blockStart, blockEnd, "Descanso", null);

        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId()))
            .thenReturn(Optional.of(mockProfessional));
        when(businessHoursRepository.findByBusinessIdAndDayOfWeek(mockBusiness.getId(), 1))
            .thenReturn(Optional.of(bHours));
        when(professionalHoursRepository.findByProfessionalIdAndDayOfWeek(mockProfessional.getId(), 1))
            .thenReturn(Optional.empty()); // Usa horario del negocio
        when(blockRepository.findOverlappingBlocks(any(), any(), any(), any()))
            .thenReturn(List.of(block));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());

        AvailabilityResponseDto response = availabilityService.getAvailability(mockService.getId(), mockProfessional.getId(), futureMonday);

        // De los 4 posibles, se excluye el slot de 08:30 a 09:00 -> quedan 3
        assertEquals(3, response.slots().size());
        assertTrue(response.slots().stream().noneMatch(s -> s.formattedTime().equals("08:30 - 09:00")));
    }

    @Test
    @DisplayName("Debe excluir slots que colisionen con una cita confirmada existente")
    void getAvailability_WithExistingAppointment_ExcludesBookedSlot() {
        BusinessHours bHours = new BusinessHours(mockBusiness, 1, LocalTime.of(8, 0), LocalTime.of(10, 0), false);

        // Cita existente de 09:00 a 09:30
        Instant apptStart = ZonedDateTime.of(futureMonday, LocalTime.of(9, 0), zoneId).toInstant();
        Instant apptEnd = ZonedDateTime.of(futureMonday, LocalTime.of(9, 30), zoneId).toInstant();
        User client = new User(mockBusiness, "Cliente Demo", "cliente@citapro.com", "1234", "pw");
        Appointment appt = new Appointment(mockBusiness, client, mockProfessional, mockService, apptStart, apptEnd, BigDecimal.valueOf(50), null);

        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId()))
            .thenReturn(Optional.of(mockProfessional));
        when(businessHoursRepository.findByBusinessIdAndDayOfWeek(mockBusiness.getId(), 1))
            .thenReturn(Optional.of(bHours));
        when(professionalHoursRepository.findByProfessionalIdAndDayOfWeek(mockProfessional.getId(), 1))
            .thenReturn(Optional.empty());
        when(blockRepository.findOverlappingBlocks(any(), any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any()))
            .thenReturn(List.of(appt));

        AvailabilityResponseDto response = availabilityService.getAvailability(mockService.getId(), mockProfessional.getId(), futureMonday);

        assertEquals(3, response.slots().size());
        assertTrue(response.slots().stream().noneMatch(s -> s.formattedTime().equals("09:00 - 09:30")));
    }

    @Test
    @DisplayName("Debe retornar lista vacía si el negocio está cerrado ese día")
    void getAvailability_BusinessClosed_ReturnsEmptySlots() {
        BusinessHours bHours = new BusinessHours(mockBusiness, 1, LocalTime.of(8, 0), LocalTime.of(18, 0), true);

        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId()))
            .thenReturn(Optional.of(mockProfessional));
        when(businessHoursRepository.findByBusinessIdAndDayOfWeek(mockBusiness.getId(), 1))
            .thenReturn(Optional.of(bHours));

        AvailabilityResponseDto response = availabilityService.getAvailability(mockService.getId(), mockProfessional.getId(), futureMonday);

        assertNotNull(response);
        assertTrue(response.slots().isEmpty());
    }
}

