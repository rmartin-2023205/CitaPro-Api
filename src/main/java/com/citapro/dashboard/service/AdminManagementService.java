package com.citapro.dashboard.service;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.availability.dto.AvailabilityBlockResponseDto;
import com.citapro.availability.dto.CreateAvailabilityBlockDto;
import com.citapro.availability.model.AvailabilityBlock;
import com.citapro.availability.repository.AvailabilityBlockRepository;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.common.exception.ResourceNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@SuppressWarnings("null")
public class AdminManagementService {

    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final AvailabilityBlockRepository blockRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    public AdminManagementService(
            BusinessRepository businessRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository,
            AvailabilityBlockRepository blockRepository,
            AppointmentRepository appointmentRepository,
            UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
        this.blockRepository = blockRepository;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
    }

    // --- SERVICIOS CRUD ---
    @Transactional(readOnly = true)
    public List<ServiceResponseDto> getAllServices() {
        Business business = getActiveBusiness();
        return serviceRepository.findByBusinessIdOrderByNameAsc(business.getId())
            .stream()
            .map(ServiceResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public ServiceResponseDto createService(CreateServiceRequestDto dto) {
        Business business = getActiveBusiness();
        ServiceEntity service = new ServiceEntity(
            business,
            dto.name().trim(),
            dto.description(),
            dto.durationMinutes(),
            dto.price()
        );
        return ServiceResponseDto.fromEntity(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponseDto updateService(UUID id, UpdateServiceRequestDto dto) {
        ServiceEntity service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (dto.name() != null && !dto.name().isBlank()) service.setName(dto.name().trim());
        if (dto.description() != null) service.setDescription(dto.description());
        if (dto.durationMinutes() != null && dto.durationMinutes() > 0) service.setDurationMinutes(dto.durationMinutes());
        if (dto.price() != null && dto.price().signum() >= 0) service.setPrice(dto.price());
        if (dto.active() != null) service.setActive(dto.active());

        return ServiceResponseDto.fromEntity(serviceRepository.save(service));
    }

    @Transactional
    public void deleteService(UUID id) {
        ServiceEntity service = serviceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        // Desactivación lógica para preservar el histórico de citas
        service.setActive(false);
        serviceRepository.save(service);
    }

    // --- PROFESIONALES CRUD ---
    @Transactional(readOnly = true)
    public List<ProfessionalResponseDto> getAllProfessionals() {
        Business business = getActiveBusiness();
        return professionalRepository.findByBusinessIdOrderByDisplayNameAsc(business.getId())
            .stream()
            .map(ProfessionalResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public ProfessionalResponseDto createProfessional(CreateProfessionalRequestDto dto) {
        Business business = getActiveBusiness();
        Professional pro = new Professional(business, dto.displayName().trim(), dto.bio());
        pro.setUserId(dto.userId());

        if (dto.serviceIds() != null && !dto.serviceIds().isEmpty()) {
            List<ServiceEntity> services = serviceRepository.findAllById(dto.serviceIds());
            pro.setServices(new HashSet<>(services));
        }

        return ProfessionalResponseDto.fromEntity(professionalRepository.save(pro));
    }

    @Transactional
    public ProfessionalResponseDto updateProfessional(UUID id, UpdateProfessionalRequestDto dto) {
        Professional pro = professionalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));

        if (dto.displayName() != null && !dto.displayName().isBlank()) pro.setDisplayName(dto.displayName().trim());
        if (dto.bio() != null) pro.setBio(dto.bio());
        if (dto.active() != null) pro.setActive(dto.active());

        if (dto.serviceIds() != null) {
            List<ServiceEntity> services = serviceRepository.findAllById(dto.serviceIds());
            pro.setServices(new HashSet<>(services));
        }

        return ProfessionalResponseDto.fromEntity(professionalRepository.save(pro));
    }

    // --- BLOQUEOS Y DESCANSOS ---
    @Transactional(readOnly = true)
    public List<AvailabilityBlockResponseDto> getAllBlocks() {
        Business business = getActiveBusiness();
        return blockRepository.findByBusinessIdOrderByStartAtDesc(business.getId())
            .stream()
            .map(AvailabilityBlockResponseDto::fromEntity)
            .toList();
    }

    @Transactional
    public AvailabilityBlockResponseDto createBlock(CreateAvailabilityBlockDto dto, String userEmail) {
        Business business = getActiveBusiness();
        User user = userRepository.findByEmailWithRoles(userEmail).orElse(null);

        Professional professional = null;
        if (dto.professionalId() != null) {
            professional = professionalRepository.findById(dto.professionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado"));
        }

        if (!dto.startAt().isBefore(dto.endAt())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la de fin");
        }

        AvailabilityBlock block = new AvailabilityBlock(
            business,
            professional,
            dto.startAt(),
            dto.endAt(),
            dto.reason(),
            user
        );

        return AvailabilityBlockResponseDto.fromEntity(blockRepository.save(block));
    }

    @Transactional
    public void deleteBlock(UUID id) {
        blockRepository.deleteById(id);
    }

    // --- CITAS GLOBALES CON FILTROS ---
    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAdminAppointments(Instant from, Instant to, UUID professionalId, AppointmentStatus status) {
        Business business = getActiveBusiness();

        Instant start = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant end = to != null ? to : Instant.now().plus(60, ChronoUnit.DAYS);

        return appointmentRepository.findAdminAppointments(business.getId(), start, end, professionalId, status)
            .stream()
            .map(AppointmentResponseDto::fromEntity)
            .toList();
    }

    private Business getActiveBusiness() {
        return businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Negocio no configurado"));
    }
}

