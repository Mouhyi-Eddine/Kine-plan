package com.kineplan.shared.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(UUID userId, UUID cabinetId, UUID membershipId, String role) {
        return createToken(userId, properties.accessTokenTtl(), "cabinet", builder -> builder
                .claim("cabinet_id", cabinetId.toString())
                .claim("membership_id", membershipId.toString())
                .claim("role", role));
    }

    public String createPreAuthToken(UUID userId) {
        return createToken(userId, properties.preAuthTokenTtl(), "pre-auth", builder -> builder);
    }

    public String createRefreshToken(UUID userId, UUID cabinetId, UUID membershipId) {
        return createToken(userId, properties.refreshTokenTtl(), "refresh", builder -> builder
                .claim("cabinet_id", cabinetId.toString())
                .claim("membership_id", membershipId.toString()));
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }

    private String createToken(UUID userId, java.time.Duration ttl, String tokenType,
                               java.util.function.UnaryOperator<io.jsonwebtoken.JwtBuilder> customizer) {
        Instant now = Instant.now();
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .subject(userId.toString())
                .claim("token_type", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey);
        return customizer.apply(builder).compact();
    }
}