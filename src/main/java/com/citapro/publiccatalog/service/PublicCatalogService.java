package com.citapro.publiccatalog.service;

import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.publiccatalog.dto.BusinessResponseDto;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.publiccatalog.dto.ServiceResponseDto;
import com.citapro.services.model.ServiceEntity;
import com.citapro.services.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PublicCatalogService {

    private final BusinessRepository businessRepository;
    private final ServiceRepository serviceRepository;
    private final ProfessionalRepository professionalRepository;

    public PublicCatalogService(
            BusinessRepository businessRepository,
            ServiceRepository serviceRepository,
            ProfessionalRepository professionalRepository) {
        this.businessRepository = businessRepository;
        this.serviceRepository = serviceRepository;
        this.professionalRepository = professionalRepository;
    }

    public BusinessResponseDto getBusiness() {
        Business business = businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("No se encontró información del negocio activo"));
        return BusinessResponseDto.fromEntity(business);
    }

    public List<ServiceResponseDto> getServices() {
        Business business = businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Negocio no configurado"));

        return serviceRepository.findByBusinessIdAndActiveTrueOrderByNameAsc(business.getId())
            .stream()
            .map(ServiceResponseDto::fromEntity)
            .toList();
    }

    public List<ProfessionalResponseDto> getProfessionals() {
        Business business = businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Negocio no configurado"));

        return professionalRepository.findByBusinessIdAndActiveTrueWithServices(business.getId())
            .stream()
            .map(ProfessionalResponseDto::fromEntity)
            .toList();
    }

    public List<ServiceResponseDto> getServicesForProfessional(UUID professionalId) {
        Professional professional = professionalRepository.findByIdAndActiveTrueWithServices(professionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado o inactivo"));

        return professional.getServices().stream()
            .filter(ServiceEntity::isActive)
            .map(ServiceResponseDto::fromEntity)
            .toList();
    }
}

