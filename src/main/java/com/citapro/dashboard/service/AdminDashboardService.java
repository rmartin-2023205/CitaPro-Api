package com.citapro.dashboard.service;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.dashboard.dto.DashboardSummaryDto;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.repository.ServiceRepository;
import com.citapro.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final BusinessRepository businessRepository;
    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;
    private final UserRepository userRepository;

    public AdminDashboardService(
            BusinessRepository businessRepository,
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository,
            UserRepository userRepository) {
        this.businessRepository = businessRepository;
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummaryDto getDashboardSummary() {
        Business business = businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Negocio no configurado"));

        ZoneId zoneId = ZoneId.of(business.getTimezone() != null ? business.getTimezone() : "America/Guatemala");
        LocalDate today = LocalDate.now(zoneId);

        Instant startOfToday = today.atStartOfDay(zoneId).toInstant();
        Instant endOfToday = today.plusDays(1).atStartOfDay(zoneId).toInstant();

        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        Instant startOfMonth = firstDayOfMonth.atStartOfDay(zoneId).toInstant();

        List<Appointment> todayAppointments = appointmentRepository.findByBusinessIdAndStartAtBetweenOrderByStartAtAsc(
            business.getId(), startOfToday, endOfToday
        );

        List<Appointment> monthAppointments = appointmentRepository.findByBusinessIdAndStartAtBetweenOrderByStartAtAsc(
            business.getId(), startOfMonth, endOfToday
        );

        long totalToday = todayAppointments.size();
        long pendingToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count();
        long confirmedToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long completedToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelledToday = todayAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();

        // Ingresos de citas completadas hoy y este mes (o confirmadas)
        BigDecimal revenueToday = todayAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CONFIRMED)
            .map(Appointment::getPriceAtBooking)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal revenueMonth = monthAppointments.stream()
            .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.CONFIRMED)
            .map(Appointment::getPriceAtBooking)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalServices = serviceRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(business.getId()).size();
        long totalProfessionals = professionalRepository.findByBusinessIdAndActiveTrueWithServices(business.getId()).size();
        long totalClients = userRepository.count();

        return new DashboardSummaryDto(
            totalToday,
            pendingToday,
            confirmedToday,
            completedToday,
            cancelledToday,
            revenueToday,
            revenueMonth,
            totalClients,
            totalServices,
            totalProfessionals
        );
    }
}
