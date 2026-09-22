package com.kineplan.cabinet.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OnboardingRequest(
        @NotBlank @Size(max = 200) String cabinetName,
        @Size(max = 500) String address,
        @Size(max = 14) String siret,
        @NotBlank @Size(max = 64) String timezone,
        @NotBlank @Email @Size(max = 320) String adminEmail,
        @NotBlank @Size(min = 12, max = 128) String adminPassword,
        @NotBlank @Size(max = 100) String adminFirstName,
        @NotBlank @Size(max = 100) String adminLastName) {
}
