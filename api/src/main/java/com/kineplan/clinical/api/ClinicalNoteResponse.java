package com.kineplan.clinical.api;

import java.util.UUID;

public record ClinicalNoteResponse(UUID id, UUID patientId, UUID appointmentId, String content) {
}