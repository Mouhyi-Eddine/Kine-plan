package com.kineplan.appointment.api;

import com.kineplan.appointment.domain.AppointmentSource;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record AppointmentRequest(
        @NotNull UUID patientId,
        @NotNull UUID practitionerMembershipId,
        UUID careTypeId,
        @NotNull Instant startAt,
        @NotNull Instant endAt,
        @NotNull AppointmentSource source) {
}