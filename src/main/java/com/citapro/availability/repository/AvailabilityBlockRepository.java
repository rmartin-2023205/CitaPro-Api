package com.citapro.availability.repository;

import com.citapro.availability.model.AvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock, UUID> {

    @Query("SELECT b FROM AvailabilityBlock b WHERE (b.business.id = :businessId AND (b.professional.id = :professionalId OR b.professional IS NULL)) AND (b.startAt < :rangeEnd AND b.endAt > :rangeStart)")
    List<AvailabilityBlock> findOverlappingBlocks(
        @Param("businessId") UUID businessId,
        @Param("professionalId") UUID professionalId,
        @Param("rangeStart") Instant rangeStart,
        @Param("rangeEnd") Instant rangeEnd
    );

    List<AvailabilityBlock> findByBusinessIdOrderByStartAtDesc(UUID businessId);
}

