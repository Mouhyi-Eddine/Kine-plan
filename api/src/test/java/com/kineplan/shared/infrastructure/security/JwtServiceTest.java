package com.kineplan.shared.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {
    private final JwtService jwtService = new JwtService(new JwtProperties(
            "01234567890123456789012345678901",
            Duration.ofMinutes(10),
            Duration.ofMinutes(2),
            Duration.ofDays(30)));

    @Test
    void accessTokenContainsOnlyTheSelectedCabinetContext() {
        UUID userId = UUID.randomUUID();
        UUID cabinetId = UUID.randomUUID();
        UUID membershipId = UUID.randomUUID();

        var claims = jwtService.parse(jwtService.createAccessToken(
                userId, cabinetId, membershipId, "ADMIN"));

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("token_type", String.class)).isEqualTo("cabinet");
        assertThat(claims.get("cabinet_id", String.class)).isEqualTo(cabinetId.toString());
        assertThat(claims.get("membership_id", String.class)).isEqualTo(membershipId.toString());
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void refreshTokenIsDistinctFromAnAccessToken() {
        var claims = jwtService.parse(jwtService.createRefreshToken(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()));

        assertThat(claims.get("token_type", String.class)).isEqualTo("refresh");
        assertThat(claims.get("role")).isNull();
    }
}