package com.citapro.appointments.service;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.dto.CancelAppointmentRequestDto;
import com.citapro.appointments.dto.CreateAppointmentRequestDto;
import com.citapro.appointments.dto.UpdateAppointmentStatusRequestDto;
import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.model.AppointmentStatusHistory;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.appointments.repository.AppointmentStatusHistoryRepository;
import com.citapro.business.model.Business;
import com.citapro.common.exception.ConflictException;
import com.citapro.common.exception.ForbiddenException;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import com.citapro.users.model.Role;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class AppointmentService {

    private static final Duration MIN_CANCEL_NOTICE = Duration.ofHours(2);

    private final AppointmentRepository appointmentRepository;
    private final AppointmentStatusHistoryRepository historyRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            AppointmentStatusHistoryRepository historyRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository) {
        this.appointmentRepository = appointmentRepository;
        this.historyRepository = historyRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponseDto createAppointment(CreateAppointmentRequestDto dto, String clientEmail) {
        User client = userRepository.findByEmailWithRoles(clientEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario cliente no encontrado"));

        ServiceEntity service = serviceRepository.findById(dto.serviceId())
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (!service.isActive()) {
            throw new IllegalArgumentException("El servicio seleccionado ya no está disponible.");
        }

        Professional professional = professionalRepository.findByIdAndActiveTrueWithServices(dto.professionalId())
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado o inactivo"));

        boolean canPerform = professional.getServices().stream().anyMatch(s -> s.getId().equals(service.getId()));
        if (!canPerform) {
            throw new IllegalArgumentException("El profesional seleccionado no realiza este servicio.");
        }

        Instant now = Instant.now();
        if (!dto.startAt().isAfter(now)) {
            throw new IllegalArgumentException("No se pueden reservar citas en fechas u horarios pasados.");
        }

        Instant endAt = dto.startAt().plus(service.getDurationMinutes(), ChronoUnit.MINUTES);

        // Verificación inicial de disponibilidad en Java (primera línea de defensa)
        List<Appointment> overlapping = appointmentRepository.findOverlappingAppointments(
            professional.getId(),
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
            dto.startAt(),
            endAt
        );

        if (!overlapping.isEmpty()) {
            throw new ConflictException("El horario seleccionado acaba de ser ocupado. Por favor elija otro horario.");
        }

        Business business = service.getBusiness();

        // Congelar precio en el momento de la reserva
        Appointment appointment = new Appointment(
            business,
            client,
            professional,
            service,
            dto.startAt(),
            endAt,
            service.getPrice(),
            dto.notes()
        );

        // saveAndFlush garantiza que la restricción de exclusión de PostgreSQL (btree_gist) se evalúe inmediatamente
        Appointment savedAppointment = appointmentRepository.saveAndFlush(appointment);

        // Registrar historial de creación
        historyRepository.save(new AppointmentStatusHistory(
            savedAppointment,
            null,
            AppointmentStatus.CONFIRMED,
            client,
            "Reserva creada por el cliente"
        ));

        return AppointmentResponseDto.fromEntity(savedAppointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getMyAppointments(String clientEmail) {
        User client = userRepository.findByEmailWithRoles(clientEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return appointmentRepository.findByClientIdOrderByStartAtDesc(client.getId())
            .stream()
            .map(AppointmentResponseDto::fromEntity)
            .toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(UUID appointmentId, String userEmail) {
        User user = userRepository.findByEmailWithRoles(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        validateAccess(appointment, user);

        return AppointmentResponseDto.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponseDto cancelAppointment(UUID appointmentId, CancelAppointmentRequestDto dto, String userEmail) {
        User user = userRepository.findByEmailWithRoles(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        validateAccess(appointment, user);

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ConflictException("La cita ya se encuentra cancelada.");
        }

        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.NO_SHOW) {
            throw new ConflictException("No es posible cancelar una cita que ya fue completada o marcada como no presentada.");
        }

        boolean isAdmin = user.getUserRoles().stream().anyMatch(ur -> ur.getRole() == Role.ROLE_ADMIN);
        boolean isAssignedPro = appointment.getProfessional().getUserId() != null &&
                                appointment.getProfessional().getUserId().equals(user.getId());

        // Si es cliente, validar política de cancelación (mínimo 2 horas de anticipación)
        if (!isAdmin && !isAssignedPro) {
            Instant cancelDeadline = appointment.getStartAt().minus(MIN_CANCEL_NOTICE);
            if (Instant.now().isAfter(cancelDeadline)) {
                throw new ConflictException("Política de cancelación: Las citas solo pueden cancelarse con al menos 2 horas de anticipación.");
            }
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        appointment.setCancelledBy(user);

        Appointment updated = appointmentRepository.save(appointment);

        String reason = (dto != null && dto.reason() != null && !dto.reason().isBlank())
            ? dto.reason()
            : "Cancelada por " + user.getFullName();

        historyRepository.save(new AppointmentStatusHistory(
            updated,
            oldStatus,
            AppointmentStatus.CANCELLED,
            user,
            reason
        ));

        return AppointmentResponseDto.fromEntity(updated);
    }

    @Transactional
    public AppointmentResponseDto updateStatus(UUID appointmentId, UpdateAppointmentStatusRequestDto dto, String userEmail) {
        User user = userRepository.findByEmailWithRoles(userEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        boolean isAdmin = user.getUserRoles().stream().anyMatch(ur -> ur.getRole() == Role.ROLE_ADMIN);
        boolean isAssignedPro = appointment.getProfessional().getUserId() != null &&
                                appointment.getProfessional().getUserId().equals(user.getId());

        if (!isAdmin && !isAssignedPro) {
            throw new ForbiddenException("Solo administradores o el especialista asignado pueden actualizar el estado de esta cita.");
        }

        AppointmentStatus oldStatus = appointment.getStatus();
        appointment.setStatus(dto.status());

        if (dto.status() == AppointmentStatus.CANCELLED) {
            appointment.setCancelledAt(Instant.now());
            appointment.setCancelledBy(user);
        }

        Appointment updated = appointmentRepository.save(appointment);

        historyRepository.save(new AppointmentStatusHistory(
            updated,
            oldStatus,
            dto.status(),
            user,
            dto.reason() != null ? dto.reason() : "Cambio de estado administrativo"
        ));

        return AppointmentResponseDto.fromEntity(updated);
    }

    private void validateAccess(Appointment appointment, User user) {
        boolean isClient = appointment.getClient().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoles().stream().anyMatch(ur -> ur.getRole() == Role.ROLE_ADMIN);
        boolean isAssignedPro = appointment.getProfessional().getUserId() != null &&
                                appointment.getProfessional().getUserId().equals(user.getId());

        if (!isClient && !isAdmin && !isAssignedPro) {
            throw new ForbiddenException("No posee permisos para acceder a los datos de esta cita.");
        }
    }
}
