package com.citapro.dashboard.dto;

import java.math.BigDecimal;

public record DashboardSummaryDto(
    long totalAppointmentsToday,
    long pendingAppointmentsToday,
    long confirmedAppointmentsToday,
    long completedAppointmentsToday,
    long cancelledAppointmentsToday,
    BigDecimal revenueToday,
    BigDecimal revenueMonth,
    long totalClients,
    long totalServices,
    long totalProfessionals
) {}
