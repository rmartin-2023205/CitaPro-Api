package com.citapro.professionals.service;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.business.model.Business;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.services.model.ServiceEntity;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfessionalAgendaServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private ProfessionalAgendaService agendaService;

    private User mockUser;
    private Professional mockProfessional;
    private Business mockBusiness;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("Barbería El Éxito", "America/Guatemala", "info@elexito.gt", "+502 2222-1111");
        mockBusiness.setId(UUID.randomUUID());

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("carlos@citapro.com");
        mockUser.setFullName("Carlos Gómez");

        mockProfessional = new Professional(mockBusiness, "Carlos Gómez", "Barbero profesional");
        mockProfessional.setId(UUID.randomUUID());
        mockProfessional.setUserId(mockUser.getId());
    }

    @Test
    @DisplayName("Debe retornar el perfil del profesional autenticado")
    void testGetMyProfileSuccess() {
        when(userRepository.findByEmailWithRoles("carlos@citapro.com")).thenReturn(Optional.of(mockUser));
        when(professionalRepository.findByUserIdAndActiveTrue(mockUser.getId())).thenReturn(Optional.of(mockProfessional));

        ProfessionalResponseDto response = agendaService.getMyProfile("carlos@citapro.com");

        assertNotNull(response);
        assertEquals(mockProfessional.getId(), response.id());
        assertEquals("Carlos Gómez", response.displayName());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el usuario no tiene perfil de profesional asignado")
    void testGetMyProfileNotFound() {
        when(userRepository.findByEmailWithRoles("carlos@citapro.com")).thenReturn(Optional.of(mockUser));
        when(professionalRepository.findByUserIdAndActiveTrue(mockUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> agendaService.getMyProfile("carlos@citapro.com"));
    }

    @Test
    @DisplayName("Debe retornar la lista de citas en la agenda del profesional")
    void testGetAgendaSuccess() {
        when(userRepository.findByEmailWithRoles("carlos@citapro.com")).thenReturn(Optional.of(mockUser));
        when(professionalRepository.findByUserIdAndActiveTrue(mockUser.getId())).thenReturn(Optional.of(mockProfessional));

        ServiceEntity service = new ServiceEntity(mockBusiness, "Corte", "Desc", 30, BigDecimal.valueOf(50.00));
        service.setId(UUID.randomUUID());

        Appointment appointment = new Appointment(
                mockBusiness,
                mockUser,
                mockProfessional,
                service,
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(30, ChronoUnit.MINUTES),
                BigDecimal.valueOf(50.00),
                "Nota"
        );
        appointment.setId(UUID.randomUUID());

        when(appointmentRepository.findByProfessionalIdAndStartAtBetweenOrderByStartAtAsc(eq(mockProfessional.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(appointment));

        List<AppointmentResponseDto> agenda = agendaService.getAgenda("carlos@citapro.com", null, null);

        assertNotNull(agenda);
        assertEquals(1, agenda.size());
        assertEquals("Corte", agenda.get(0).serviceName());
    }
}
