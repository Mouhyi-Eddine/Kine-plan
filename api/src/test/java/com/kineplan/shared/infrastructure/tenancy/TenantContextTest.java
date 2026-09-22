package com.kineplan.shared.infrastructure.tenancy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TenantContextTest {
    @AfterEach
    void clearContext() {
        TenantContext.clear();
    }

    @Test
    void rejectsMissingTenantAndClearsTenant() {
        assertThatThrownBy(TenantContext::getRequired)
                .isInstanceOf(TenantNotSetException.class);

        UUID cabinetId = UUID.randomUUID();
        TenantContext.set(cabinetId);
        assertThat(TenantContext.getRequired()).isEqualTo(cabinetId);

        TenantContext.clear();
        assertThat(TenantContext.getOrNull()).isNull();
    }
}