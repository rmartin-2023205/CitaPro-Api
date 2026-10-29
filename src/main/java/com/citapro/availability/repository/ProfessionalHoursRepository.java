package com.citapro.availability.repository;

import com.citapro.availability.model.ProfessionalHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfessionalHoursRepository extends JpaRepository<ProfessionalHours, UUID> {
    Optional<ProfessionalHours> findByProfessionalIdAndDayOfWeek(UUID professionalId, int dayOfWeek);
}

