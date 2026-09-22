package com.kineplan.shared.infrastructure.tenancy;

import java.util.UUID;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class HibernateTenantIdentifierResolver implements CurrentTenantIdentifierResolver<UUID> {
    public static final UUID PLATFORM_TENANT = new UUID(0, 0);

    @Override
    public UUID resolveCurrentTenantIdentifier() {
        UUID tenant = TenantContext.getOrNull();
        return tenant == null ? PLATFORM_TENANT : tenant;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}