package com.citapro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
    @NotBlank(message = "El nombre completo es requerido")
    @Size(min = 2, max = 150, message = "El nombre debe tener entre 2 y 150 caracteres")
    String fullName,

    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    String phone
) {}

