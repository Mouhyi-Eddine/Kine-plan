package com.kineplan.appointment.api;

import com.kineplan.appointment.domain.AppointmentSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

public record AppointmentSeriesRequest(
        @NotNull UUID patientId,
        @NotNull UUID practitionerMembershipId,
        UUID careTypeId,
        @NotNull Instant firstStartAt,
        @Min(1) int durationMinutes,
        @Min(1) @Max(100) int occurrences,
        @Min(1) @Max(31) int intervalDays,
        @NotNull AppointmentSource source) {
}