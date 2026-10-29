package com.citapro.users.model;

public enum Role {
    ROLE_ADMIN,
    ROLE_PROFESSIONAL,
    ROLE_CLIENT;

    public String getCleanName() {
        return name().replace("ROLE_", "");
    }

    public static Role fromString(String roleStr) {
        if (roleStr == null) return ROLE_CLIENT;
        String normalized = roleStr.toUpperCase().trim();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return Role.valueOf(normalized);
    }
}

