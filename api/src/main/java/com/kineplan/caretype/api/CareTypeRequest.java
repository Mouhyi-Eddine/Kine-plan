package com.kineplan.caretype.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CareTypeRequest(@NotBlank String name, @Min(1) int defaultDurationMinutes, String displayColor) {
}