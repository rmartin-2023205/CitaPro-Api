package com.citapro.appointments.model;

public enum AppointmentStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    NO_SHOW;

    public boolean isBlocking() {
        return this == PENDING || this == CONFIRMED;
    }
}

