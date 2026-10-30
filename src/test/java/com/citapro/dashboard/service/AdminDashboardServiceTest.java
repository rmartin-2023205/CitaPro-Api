package com.citapro.dashboard.service;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.dashboard.dto.DashboardSummaryDto;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private ProfessionalRepository professionalRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminDashboardService dashboardService;

    private Business mockBusiness;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("Barbería El Éxito", "America/Guatemala", "info@elexito.gt", "+502 2222-1111");
        mockBusiness.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Debe calcular métricas del dashboard correctamente")
    void testGetDashboardSummarySuccess() {
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));

        Appointment apt1 = new Appointment();
        apt1.setStatus(AppointmentStatus.CONFIRMED);
        apt1.setPriceAtBooking(BigDecimal.valueOf(100.00));

        Appointment apt2 = new Appointment();
        apt2.setStatus(AppointmentStatus.COMPLETED);
        apt2.setPriceAtBooking(BigDecimal.valueOf(150.00));

        when(appointmentRepository.findByBusinessIdAndStartAtBetweenOrderByStartAtAsc(eq(mockBusiness.getId()), any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(apt1, apt2));

        when(serviceRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(mockBusiness.getId()))
                .thenReturn(List.of(new ServiceEntity(), new ServiceEntity()));
        when(professionalRepository.findByBusinessIdAndActiveTrueWithServices(mockBusiness.getId()))
                .thenReturn(List.of(new Professional()));
        when(userRepository.count()).thenReturn(15L);

        DashboardSummaryDto summary = dashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(2, summary.totalAppointmentsToday());
        assertEquals(1, summary.confirmedAppointmentsToday());
        assertEquals(1, summary.completedAppointmentsToday());
        assertEquals(0, summary.pendingAppointmentsToday());
        assertEquals(0, summary.cancelledAppointmentsToday());
        assertEquals(BigDecimal.valueOf(250.00), summary.revenueToday());
        assertEquals(BigDecimal.valueOf(250.00), summary.revenueMonth());
        assertEquals(15, summary.totalClients());
        assertEquals(2, summary.totalServices());
        assertEquals(1, summary.totalProfessionals());
    }
}
