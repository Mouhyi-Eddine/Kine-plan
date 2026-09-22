package com.kineplan.clinical.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PrescriptionRequest(@NotBlank String prescriber, @NotNull LocalDate prescribedAt,
                                  @Min(1) int sessionsPrescribed, LocalDate expiresAt) {
}