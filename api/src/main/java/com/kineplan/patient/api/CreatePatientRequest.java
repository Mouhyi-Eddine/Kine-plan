package com.kineplan.patient.api;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;

public record CreatePatientRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        LocalDate birthDate,
        String phone,
        String email,
        Map<String, String> emergencyContact,
        boolean smsConsent,
        boolean emailConsent,
        boolean rgpdConsent) {
}