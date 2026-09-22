package com.kineplan.patient.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String phone,
        String email,
        Map<String, String> emergencyContact,
        boolean smsConsent,
        boolean emailConsent,
        boolean rgpdConsent,
        Instant archivedAt) {
}