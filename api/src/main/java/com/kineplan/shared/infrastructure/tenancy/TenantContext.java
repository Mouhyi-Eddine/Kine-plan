package com.kineplan.shared.infrastructure.tenancy;

import java.util.UUID;

public final class TenantContext {
    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID cabinetId) {
        CURRENT.set(cabinetId);
    }

    public static UUID getRequired() {
        UUID cabinetId = CURRENT.get();
        if (cabinetId == null) {
            throw new TenantNotSetException();
        }
        return cabinetId;
    }

    public static UUID getOrNull() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}