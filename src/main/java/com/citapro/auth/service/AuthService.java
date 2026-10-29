package com.citapro.auth.service;

import com.citapro.auth.dto.LoginRequestDto;
import com.citapro.auth.dto.RegisterRequestDto;
import com.citapro.auth.dto.UserResponseDto;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.common.exception.ResourceNotFoundException;
import com.citapro.users.model.Role;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BusinessRepository businessRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(
            UserRepository userRepository,
            BusinessRepository businessRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.businessRepository = businessRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Transactional
    public UserResponseDto register(RegisterRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        String normalizedEmail = dto.email().toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalArgumentException("Ya existe una cuenta registrada con este correo electrónico");
        }

        Business business = businessRepository.findFirstByActiveTrue()
            .orElseThrow(() -> new ResourceNotFoundException("Negocio no configurado en el sistema"));

        String encodedPassword = passwordEncoder.encode(dto.password());
        User user = new User(business, dto.fullName().trim(), normalizedEmail, dto.phone(), encodedPassword);
        user.addRole(Role.ROLE_CLIENT);

        User savedUser = userRepository.save(user);

        // Iniciar sesión automáticamente tras registro exitoso
        authenticateAndSetSession(normalizedEmail, dto.password(), request, response);

        return UserResponseDto.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDto login(LoginRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        String normalizedEmail = dto.email().toLowerCase().trim();

        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, dto.password())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            User user = userRepository.findByEmailWithRoles(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

            return UserResponseDto.fromEntity(user);
        } catch (BadCredentialsException ex) {
            throw new IllegalArgumentException("Credenciales inválidas. Verifique su correo y contraseña.");
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No hay una sesión activa");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmailWithRoles(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado para la sesión actual"));

        return UserResponseDto.fromEntity(user);
    }

    private void authenticateAndSetSession(String email, String rawPassword, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, rawPassword)
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }
}

