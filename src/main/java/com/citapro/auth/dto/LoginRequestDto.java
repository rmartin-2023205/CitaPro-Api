package com.citapro.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
    @NotBlank(message = "El correo electrónico es requerido")
    @Email(message = "Formato de correo electrónico inválido")
    String email,

    @NotBlank(message = "La contraseña es requerida")
    String password
) {}

