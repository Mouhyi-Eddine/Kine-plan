package com.kineplan.clinical.api;

import java.time.LocalDate;
import java.util.UUID;

public record PrescriptionResponse(UUID id, UUID patientId, String prescriber, LocalDate prescribedAt,
                                   int sessionsPrescribed, int sessionsConsumed, LocalDate expiresAt) {
}