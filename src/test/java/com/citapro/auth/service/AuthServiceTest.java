package com.citapro.auth.service;

import com.citapro.auth.dto.LoginRequestDto;
import com.citapro.auth.dto.RegisterRequestDto;
import com.citapro.auth.dto.UserResponseDto;
import com.citapro.business.model.Business;
import com.citapro.business.repository.BusinessRepository;
import com.citapro.users.model.Role;
import com.citapro.users.model.User;
import com.citapro.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BusinessRepository businessRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private Business mockBusiness;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockBusiness = new Business("CitaPro Demo", "citapro-demo", "America/Guatemala", "12345678", "demo@citapro.com", "Zona 1");
        mockBusiness.setId(UUID.randomUUID());

        mockUser = new User(mockBusiness, "Test User", "test@citapro.com", "12345678", "encoded_pw");
        mockUser.setId(UUID.randomUUID());
        mockUser.addRole(Role.ROLE_CLIENT);
    }

    @Test
    @DisplayName("Debe registrar exitosamente un nuevo usuario con rol CLIENT")
    void register_Success() {
        RegisterRequestDto dto = new RegisterRequestDto("Test User", "test@citapro.com", "Password123!", "12345678");

        when(userRepository.existsByEmailIgnoreCase("test@citapro.com")).thenReturn(false);
        when(businessRepository.findFirstByActiveTrue()).thenReturn(Optional.of(mockBusiness));
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_pw");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        UserResponseDto result = authService.register(dto, request, response);

        assertNotNull(result);
        assertEquals("test@citapro.com", result.email());
        assertTrue(result.roles().contains("CLIENT"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Debe fallar el registro si el correo ya existe")
    void register_EmailAlreadyExists() {
        RegisterRequestDto dto = new RegisterRequestDto("Test User", "test@citapro.com", "Password123!", "12345678");

        when(userRepository.existsByEmailIgnoreCase("test@citapro.com")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authService.register(dto, request, response));

        assertTrue(ex.getMessage().contains("Ya existe una cuenta"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe autenticar correctamente con login válido")
    void login_Success() {
        LoginRequestDto dto = new LoginRequestDto("test@citapro.com", "Password123!");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userRepository.findByEmailWithRoles("test@citapro.com")).thenReturn(Optional.of(mockUser));

        UserResponseDto result = authService.login(dto, request, response);

        assertNotNull(result);
        assertEquals("test@citapro.com", result.email());
        verify(securityContextRepository).saveContext(any(), eq(request), eq(response));
    }

    @Test
    @DisplayName("Debe lanzar excepción si las credenciales son incorrectas")
    void login_BadCredentials() {
        LoginRequestDto dto = new LoginRequestDto("test@citapro.com", "WrongPassword");

        when(authenticationManager.authenticate(any()))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> authService.login(dto, request, response));

        assertTrue(ex.getMessage().contains("Credenciales inválidas"));
    }

    @Test
    @DisplayName("Debe retornar los datos del usuario en sesión actual")
    void getCurrentUser_Success() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@citapro.com");
        when(userRepository.findByEmailWithRoles("test@citapro.com")).thenReturn(Optional.of(mockUser));

        UserResponseDto result = authService.getCurrentUser(authentication);

        assertNotNull(result);
        assertEquals("Test User", result.fullName());
        assertEquals("test@citapro.com", result.email());
    }
}

