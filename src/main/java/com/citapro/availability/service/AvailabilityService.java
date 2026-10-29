package com.citapro.availability.service;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.availability.dto.AvailabilityResponseDto;
import com.citapro.availability.dto.TimeSlotDto;
import com.citapro.availability.model.AvailabilityBlock;
import com.citapro.availability.model.BusinessHours;
import com.citapro.availability.model.ProfessionalHours;
import com.citapro.availability.repository.AvailabilityBlockRepository;
import com.citapro.availability.repository.BusinessHoursRepository;
import com.citapro.availability.repository.ProfessionalHoursRepository;
import com.citapro.business.model.Business;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class AvailabilityService {

    private static final int SLOT_STEP_MINUTES = 30;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final BusinessHoursRepository businessHoursRepository;
    private final ProfessionalHoursRepository professionalHoursRepository;
    private final AvailabilityBlockRepository blockRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;

    public AvailabilityService(
            BusinessHoursRepository businessHoursRepository,
            ProfessionalHoursRepository professionalHoursRepository,
            AvailabilityBlockRepository blockRepository,
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository) {
        this.businessHoursRepository = businessHoursRepository;
        this.professionalHoursRepository = professionalHoursRepository;
        this.blockRepository = blockRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
    }

    public AvailabilityResponseDto getAvailability(UUID serviceId, UUID professionalId, LocalDate date) {
        ServiceEntity service = serviceRepository.findById(serviceId)
            .orElseThrow(() -> new ResourceNotFoundException("Servicio no encontrado"));

        if (!service.isActive()) {
            throw new IllegalArgumentException("El servicio solicitado no está activo.");
        }

        Business business = service.getBusiness();
        ZoneId zoneId = ZoneId.of(business.getTimezone() != null ? business.getTimezone() : "America/Guatemala");
        LocalDate today = LocalDate.now(zoneId);

        if (date.isBefore(today)) {
            throw new IllegalArgumentException("No se pueden consultar horarios para fechas pasadas.");
        }

        // Determinar profesionales que atienden este servicio
        List<Professional> professionalsToEvaluate;
        if (professionalId != null) {
            Professional pro = professionalRepository.findByIdAndActiveTrueWithServices(professionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado o inactivo"));

            boolean canPerform = pro.getServices().stream().anyMatch(s -> s.getId().equals(serviceId));
            if (!canPerform) {
                throw new IllegalArgumentException("El profesional seleccionado no ofrece este servicio.");
            }
            professionalsToEvaluate = List.of(pro);
        } else {
            professionalsToEvaluate = professionalRepository.findByServiceIdAndActiveTrue(service.getId());
        }

        int dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Lunes, 7 = Domingo

        // Consultar horario del negocio para ese día
        Optional<BusinessHours> bHoursOpt = businessHoursRepository.findByBusinessIdAndDayOfWeek(business.getId(), dayOfWeek);
        if (bHoursOpt.isEmpty() || bHoursOpt.get().isClosed()) {
            return new AvailabilityResponseDto(
                date, service.getId(), service.getName(), service.getDurationMinutes(), zoneId.getId(), List.of()
            );
        }

        BusinessHours bHours = bHoursOpt.get();
        LocalTime bOpens = bHours.getOpensAt();
        LocalTime bCloses = bHours.getClosesAt();

        Instant startOfDay = date.atStartOfDay(zoneId).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        Instant now = Instant.now();

        List<TimeSlotDto> availableSlots = new ArrayList<>();
        int durationMinutes = service.getDurationMinutes();

        for (Professional pro : professionalsToEvaluate) {
            // Horario del profesional
            Optional<ProfessionalHours> pHoursOpt = professionalHoursRepository.findByProfessionalIdAndDayOfWeek(pro.getId(), dayOfWeek);
            if (pHoursOpt.isPresent() && pHoursOpt.get().isClosed()) {
                continue; // Profesional no labora este día
            }

            LocalTime pOpens = pHoursOpt.map(ProfessionalHours::getOpensAt).orElse(bOpens);
            LocalTime pCloses = pHoursOpt.map(ProfessionalHours::getClosesAt).orElse(bCloses);

            LocalTime effectiveStart = pOpens.isAfter(bOpens) ? pOpens : bOpens;
            LocalTime effectiveEnd = pCloses.isBefore(bCloses) ? pCloses : bCloses;

            if (!effectiveStart.isBefore(effectiveEnd)) {
                continue;
            }

            // Consultar bloqueos y citas del profesional
            List<AvailabilityBlock> blocks = blockRepository.findOverlappingBlocks(business.getId(), pro.getId(), startOfDay, endOfDay);
            List<Appointment> appointments = appointmentRepository.findOverlappingAppointments(
                pro.getId(),
                List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED),
                startOfDay,
                endOfDay
            );

            LocalTime slotStart = effectiveStart;

            while (!slotStart.plusMinutes(durationMinutes).isAfter(effectiveEnd)) {
                LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

                ZonedDateTime slotStartZoned = ZonedDateTime.of(date, slotStart, zoneId);
                ZonedDateTime slotEndZoned = ZonedDateTime.of(date, slotEnd, zoneId);

                Instant slotStartInstant = slotStartZoned.toInstant();
                Instant slotEndInstant = slotEndZoned.toInstant();

                // Regla obligatoria 1: No ofrecer horarios pasados si la fecha es hoy
                if (date.equals(today) && !slotStartInstant.isAfter(now)) {
                    slotStart = slotStart.plusMinutes(SLOT_STEP_MINUTES);
                    continue;
                }

                // Regla obligatoria 4: Respetar bloqueos y descansos
                boolean collidesWithBlock = blocks.stream().anyMatch(b ->
                    slotStartInstant.isBefore(b.getEndAt()) && slotEndInstant.isAfter(b.getStartAt())
                );

                // Regla obligatoria 6: Respetar citas activas ya reservadas
                boolean collidesWithAppointment = appointments.stream().anyMatch(a ->
                    slotStartInstant.isBefore(a.getEndAt()) && slotEndInstant.isAfter(a.getStartAt())
                );

                if (!collidesWithBlock && !collidesWithAppointment) {
                    String formatted = String.format("%s - %s",
                        slotStart.format(TIME_FORMATTER),
                        slotEnd.format(TIME_FORMATTER)
                    );

                    availableSlots.add(new TimeSlotDto(
                        pro.getId(),
                        pro.getDisplayName(),
                        slotStartInstant,
                        slotEndInstant,
                        formatted,
                        true
                    ));
                }

                slotStart = slotStart.plusMinutes(SLOT_STEP_MINUTES);
            }
        }

        // Ordenar slots por hora de inicio y nombre de profesional
        availableSlots.sort(Comparator.comparing(TimeSlotDto::startAt)
            .thenComparing(TimeSlotDto::professionalName));

        return new AvailabilityResponseDto(
            date,
            service.getId(),
            service.getName(),
            service.getDurationMinutes(),
            zoneId.getId(),
            availableSlots
        );
    }
}

