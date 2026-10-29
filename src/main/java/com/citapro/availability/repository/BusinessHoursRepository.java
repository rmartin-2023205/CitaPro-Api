package com.citapro.availability.repository;

import com.citapro.availability.model.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusinessHoursRepository extends JpaRepository<BusinessHours, UUID> {
    Optional<BusinessHours> findByBusinessIdAndDayOfWeek(UUID businessId, int dayOfWeek);
}

