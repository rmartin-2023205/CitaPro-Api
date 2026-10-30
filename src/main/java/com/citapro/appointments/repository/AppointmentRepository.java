package com.citapro.appointments.repository;

import com.citapro.appointments.model.Appointment;
import com.citapro.appointments.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT a FROM Appointment a WHERE a.professional.id = :professionalId AND a.status IN :statuses AND (a.startAt < :rangeEnd AND a.endAt > :rangeStart)")
    List<Appointment> findOverlappingAppointments(
        @Param("professionalId") UUID professionalId,
        @Param("statuses") Collection<AppointmentStatus> statuses,
        @Param("rangeStart") Instant rangeStart,
        @Param("rangeEnd") Instant rangeEnd
    );

    List<Appointment> findByClientIdOrderByStartAtDesc(UUID clientId);

    List<Appointment> findByProfessionalIdAndStartAtBetweenOrderByStartAtAsc(UUID professionalId, Instant from, Instant to);

    List<Appointment> findByBusinessIdAndStartAtBetweenOrderByStartAtAsc(UUID businessId, Instant from, Instant to);

    @Query("SELECT a FROM Appointment a WHERE a.business.id = :businessId " +
           "AND a.startAt >= :from AND a.startAt <= :to " +
           "AND (:professionalId IS NULL OR a.professional.id = :professionalId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "ORDER BY a.startAt ASC")
    List<Appointment> findAdminAppointments(
        @Param("businessId") UUID businessId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("professionalId") UUID professionalId,
        @Param("status") AppointmentStatus status
    );
}

