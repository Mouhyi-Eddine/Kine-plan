package com.kineplan.shared.infrastructure.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kineplan.security.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration preAuthTokenTtl) {
    public JwtProperties {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(10);
        }
        if (preAuthTokenTtl == null) {
            preAuthTokenTtl = Duration.ofMinutes(2);
        }
    }
}