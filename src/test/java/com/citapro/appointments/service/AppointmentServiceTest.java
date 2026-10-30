package com.citapro.appointments.service;

import com.citapro.appointments.dto.CancelAppointmentRequestDto;
import com.citapro.appointments.dto.CreateAppointmentRequestDto;
import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.appointments.repository.AppointmentStatusHistoryRepository;
import com.citapro.business.model.Business;
import com.citapro.common.exception.ConflictException;
import com.citapro.common.exception.ForbiddenException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import com.citapro.users.model.Role;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AppointmentStatusHistoryRepository historyRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private User mockClient;
    private User otherClient;
    private Business mockBusiness;
    private ServiceEntity mockService;
    private Professional mockProfessional;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("Barbería Test", "barberia-test", "America/Guatemala", "1234", "test@citapro.com", "Zona 1");
        mockBusiness.setId(UUID.randomUUID());

        mockClient = new User(mockBusiness, "Cliente Test", "cliente@citapro.com", "1234", "pw");
        mockClient.setId(UUID.randomUUID());
        mockClient.addRole(Role.ROLE_CLIENT);

        otherClient = new User(mockBusiness, "Otro Cliente", "otro@citapro.com", "5678", "pw");
        otherClient.setId(UUID.randomUUID());
        otherClient.addRole(Role.ROLE_CLIENT);

        mockService = new ServiceEntity(mockBusiness, "Corte Clásico", "Corte 30m", 30, BigDecimal.valueOf(50.00));
        mockService.setId(UUID.randomUUID());

        mockProfessional = new Professional(mockBusiness, "Carlos Gómez", "Master Barber");
        mockProfessional.setId(UUID.randomUUID());
        mockProfessional.setServices(Set.of(mockService));
    }

    @Test
    @DisplayName("Debe crear una cita exitosamente congelando el precio del servicio")
    void createAppointment_Success() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(2));
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
            mockService.getId(),
            mockProfessional.getId(),
            futureStart,
            "Primer corte"
        );

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId())).thenReturn(Optional.of(mockProfessional));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        Appointment savedMock = new Appointment(
            mockBusiness, mockClient, mockProfessional, mockService, futureStart, futureStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50.00), "Primer corte"
        );
        savedMock.setId(UUID.randomUUID());

        when(appointmentRepository.saveAndFlush(any(Appointment.class))).thenReturn(savedMock);

        AppointmentResponseDto response = appointmentService.createAppointment(dto, mockClient.getEmail());

        assertNotNull(response);
        assertEquals(BigDecimal.valueOf(50.00), response.priceAtBooking());
        assertEquals(AppointmentStatus.CONFIRMED, response.status());
        verify(historyRepository).save(any());
    }

    @Test
    @DisplayName("Debe rechazar cita con fecha en el pasado")
    void createAppointment_PastDate_ThrowsException() {
        Instant pastStart = Instant.now().minus(Duration.ofHours(1));
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
            mockService.getId(),
            mockProfessional.getId(),
            pastStart,
            null
        );

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId())).thenReturn(Optional.of(mockProfessional));

        assertThrows(IllegalArgumentException.class, () ->
            appointmentService.createAppointment(dto, mockClient.getEmail())
        );
    }

    @Test
    @DisplayName("Debe lanzar ConflictException si el horario ya está ocupado")
    void createAppointment_Conflict_ThrowsConflictException() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(2));
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
            mockService.getId(),
            mockProfessional.getId(),
            futureStart,
            null
        );

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId())).thenReturn(Optional.of(mockProfessional));

        Appointment existing = new Appointment(mockBusiness, otherClient, mockProfessional, mockService, futureStart, futureStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50), null);
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any())).thenReturn(List.of(existing));

        assertThrows(ConflictException.class, () ->
            appointmentService.createAppointment(dto, mockClient.getEmail())
        );
    }

    @Test
    @DisplayName("Debe simular colisión concurrente en PostgreSQL lanzando DataIntegrityViolationException")
    void createAppointment_PostgresExclusionConstraintViolation() {
        Instant futureStart = Instant.now().plus(Duration.ofDays(2));
        CreateAppointmentRequestDto dto = new CreateAppointmentRequestDto(
            mockService.getId(),
            mockProfessional.getId(),
            futureStart,
            null
        );

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(serviceRepository.findById(mockService.getId())).thenReturn(Optional.of(mockService));
        when(professionalRepository.findByIdAndActiveTrueWithServices(mockProfessional.getId())).thenReturn(Optional.of(mockProfessional));
        when(appointmentRepository.findOverlappingAppointments(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        // Simulamos que dos hilos simultáneos pasaron el check de Java, pero la base de datos disparó la restricción de exclusión
        when(appointmentRepository.saveAndFlush(any(Appointment.class)))
            .thenThrow(new DataIntegrityViolationException("ERROR: conflicting key value violates exclusion constraint \"no_overlapping_appointments\""));

        assertThrows(DataIntegrityViolationException.class, () ->
            appointmentService.createAppointment(dto, mockClient.getEmail())
        );
    }

    @Test
    @DisplayName("Debe denegar acceso si un cliente intenta ver la cita de otro")
    void getAppointmentById_OtherUser_ThrowsForbidden() {
        UUID appointmentId = UUID.randomUUID();
        Appointment appointment = new Appointment(mockBusiness, otherClient, mockProfessional, mockService, Instant.now().plus(Duration.ofDays(2)), Instant.now().plus(Duration.ofDays(2)).plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50), null);
        appointment.setId(appointmentId);

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(ForbiddenException.class, () ->
            appointmentService.getAppointmentById(appointmentId, mockClient.getEmail())
        );
    }

    @Test
    @DisplayName("Debe permitir cancelar una cita si faltan más de 2 horas (política del negocio)")
    void cancelAppointment_Success() {
        UUID appointmentId = UUID.randomUUID();
        Instant futureStart = Instant.now().plus(Duration.ofHours(5)); // Faltan 5 horas (>2h)
        Appointment appointment = new Appointment(mockBusiness, mockClient, mockProfessional, mockService, futureStart, futureStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50), null);
        appointment.setId(appointmentId);

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        AppointmentResponseDto result = appointmentService.cancelAppointment(
            appointmentId,
            new CancelAppointmentRequestDto("Imprevisto personal"),
            mockClient.getEmail()
        );

        assertNotNull(result);
        assertEquals(AppointmentStatus.CANCELLED, result.status());
        verify(historyRepository).save(any());
    }

    @Test
    @DisplayName("Debe rechazar cancelación de cliente si faltan menos de 2 horas")
    void cancelAppointment_TooLate_ThrowsConflict() {
        UUID appointmentId = UUID.randomUUID();
        Instant soonStart = Instant.now().plus(Duration.ofMinutes(45)); // Faltan 45 minutos (<2h)
        Appointment appointment = new Appointment(mockBusiness, mockClient, mockProfessional, mockService, soonStart, soonStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50), null);
        appointment.setId(appointmentId);

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        ConflictException ex = assertThrows(ConflictException.class, () ->
            appointmentService.cancelAppointment(appointmentId, null, mockClient.getEmail())
        );

        assertTrue(ex.getMessage().contains("al menos 2 horas de anticipación"));
    }

    @Test
    @DisplayName("Debe rechazar cancelar una cita que ya fue cancelada")
    void cancelAppointment_AlreadyCancelled_ThrowsConflict() {
        UUID appointmentId = UUID.randomUUID();
        Instant futureStart = Instant.now().plus(Duration.ofHours(5));
        Appointment appointment = new Appointment(mockBusiness, mockClient, mockProfessional, mockService, futureStart, futureStart.plus(30, ChronoUnit.MINUTES), BigDecimal.valueOf(50), null);
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.CANCELLED);

        when(userRepository.findByEmailWithRoles(mockClient.getEmail())).thenReturn(Optional.of(mockClient));
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        assertThrows(ConflictException.class, () ->
            appointmentService.cancelAppointment(appointmentId, null, mockClient.getEmail())
        );
    }
}
