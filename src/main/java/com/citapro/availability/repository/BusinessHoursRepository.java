package com.citapro.availability.repository;

import com.citapro.availability.model.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    Optional<BusinessHours> findByBusinessIdAndDayOfWeek(UUID businessId, int dayOfWeek);
}

