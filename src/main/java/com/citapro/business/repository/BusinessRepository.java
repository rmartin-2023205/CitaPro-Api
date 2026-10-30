package com.citapro.business.repository;

import com.citapro.business.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
    Optional<Business> findBySlugAndActiveTrue(String slug);
    Optional<Business> findFirstByActiveTrue();
}

