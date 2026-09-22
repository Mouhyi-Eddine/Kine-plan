package com.kineplan.cabinet.api;

import com.kineplan.cabinet.domain.CabinetStatus;
import java.time.Instant;
import java.util.UUID;

public record CabinetResponse(
        UUID id,
        String name,
        String address,
        String siret,
        String timezone,
        CabinetStatus status,
        String subscriptionPlan,
        Instant createdAt) {
}
