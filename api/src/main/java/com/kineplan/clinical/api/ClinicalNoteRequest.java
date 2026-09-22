package com.kineplan.clinical.api;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record ClinicalNoteRequest(@NotBlank String content, UUID appointmentId) {
}