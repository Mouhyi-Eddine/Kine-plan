package com.kineplan.cabinet.api;

public record CabinetQuotasResponse(
        String subscriptionPlan,
        long activePractitioners,
        long maxPractitioners,
        long smsSentThisMonth,
        long maxSmsPerMonth) {
}
