package com.citapro.services.repository;

import com.citapro.services.model.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByBusinessIdAndActiveTrueOrderByNameAsc(UUID businessId);
}

