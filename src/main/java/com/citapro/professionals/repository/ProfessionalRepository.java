package com.citapro.professionals.repository;

import com.citapro.professionals.model.Professional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfessionalRepository extends JpaRepository<Professional, UUID> {

    @Query("SELECT DISTINCT p FROM Professional p LEFT JOIN FETCH p.services s WHERE p.business.id = :businessId AND p.active = true ORDER BY p.displayName ASC")
    List<Professional> findByBusinessIdAndActiveTrueWithServices(@Param("businessId") UUID businessId);

    @Query("SELECT p FROM Professional p LEFT JOIN FETCH p.services WHERE p.id = :id AND p.active = true")
    Optional<Professional> findByIdAndActiveTrueWithServices(@Param("id") UUID id);

    @Query("SELECT DISTINCT p FROM Professional p JOIN p.services s WHERE s.id = :serviceId AND p.active = true ORDER BY p.displayName ASC")
    List<Professional> findByServiceIdAndActiveTrue(@Param("serviceId") UUID serviceId);

    List<Professional> findByBusinessIdOrderByDisplayNameAsc(UUID businessId);

    Optional<Professional> findByUserIdAndActiveTrue(UUID userId);
}

