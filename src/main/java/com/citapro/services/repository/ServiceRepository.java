package com.citapro.services.repository;

import com.citapro.services.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByBusinessIdAndActiveTrueOrderByNameAsc(UUID businessId);
    List<ServiceEntity> findByBusinessIdOrderByNameAsc(UUID businessId);
}

