package com.citapro.availability.repository;

import com.citapro.availability.model.ProfessionalHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProfessionalHoursRepository extends JpaRepository<ProfessionalHours, UUID> {
    Optional<ProfessionalHours> findByProfessionalIdAndDayOfWeek(UUID professionalId, int dayOfWeek);
}

