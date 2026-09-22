package com.kineplan.cabinet.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateCabinetRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 500) String address,
        @Pattern(regexp = "^$|\\d{14}", message = "SIRET must contain 14 digits") String siret,
        @NotBlank @Size(max = 64) String timezone) {
}
