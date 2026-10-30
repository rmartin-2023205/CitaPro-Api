package com.citapro.auth.dto;

import com.citapro.users.model.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponseDto(
    UUID id,
    UUID businessId,
    String fullName,
    String email,
    String phone,
    Set<String> roles
) {
    public static UserResponseDto fromEntity(User user) {
        Set<String> roleNames = user.getUserRoles().stream()
            .map(ur -> ur.getRole().getCleanName())
            .collect(Collectors.toSet());

        return new UserResponseDto(
            user.getId(),
            user.getBusiness() != null ? user.getBusiness().getId() : null,
            user.getFullName(),
            user.getEmail(),
            user.getPhone(),
            roleNames
        );
    }
}

