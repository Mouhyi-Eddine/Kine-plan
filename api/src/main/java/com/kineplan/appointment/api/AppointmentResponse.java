package com.kineplan.appointment.api;

import com.kineplan.appointment.domain.AppointmentSource;
import com.kineplan.appointment.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        UUID practitionerMembershipId,
        UUID careTypeId,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status,
        AppointmentSource source,
        String cancellationReason) {
}