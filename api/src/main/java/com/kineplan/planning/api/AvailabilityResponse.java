package com.kineplan.planning.api;

import java.time.Instant;

public record AvailabilityResponse(Instant startAt, Instant endAt) {
}