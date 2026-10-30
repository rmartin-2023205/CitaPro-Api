package com.citapro.professionals.service;

import com.citapro.appointments.dto.AppointmentResponseDto;
import com.citapro.appointments.repository.AppointmentRepository;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.professionals.model.Professional;
import com.citapro.professionals.repository.ProfessionalRepository;
import com.citapro.publiccatalog.dto.ProfessionalResponseDto;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ProfessionalAgendaService {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final AppointmentRepository appointmentRepository;

    public ProfessionalAgendaService(
            UserRepository userRepository,
            ProfessionalRepository professionalRepository,
            AppointmentRepository appointmentRepository) {
        this.userRepository = userRepository;
        this.professionalRepository = professionalRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public ProfessionalResponseDto getMyProfile(String userEmail) {
        User user = userRepository.findByEmailWithRoles(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Professional pro = professionalRepository.findByUserIdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de especialista asignado a este usuario"));

        return ProfessionalResponseDto.fromEntity(pro);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponseDto> getAgenda(String userEmail, Instant from, Instant to) {
        User user = userRepository.findByEmailWithRoles(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        Professional pro = professionalRepository.findByUserIdAndActiveTrue(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró perfil de especialista asignado a este usuario"));

        Instant effectiveFrom = (from != null) ? from : Instant.now().minus(7, ChronoUnit.DAYS);
        Instant effectiveTo = (to != null) ? to : Instant.now().plus(30, ChronoUnit.DAYS);

        return appointmentRepository.findByProfessionalIdAndStartAtBetweenOrderByStartAtAsc(pro.getId(), effectiveFrom, effectiveTo)
                .stream()
                .map(AppointmentResponseDto::fromEntity)
                .toList();
    }
}
