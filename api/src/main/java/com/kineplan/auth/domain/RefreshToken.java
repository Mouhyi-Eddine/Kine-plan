package com.kineplan.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    private UUID id;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Column(name = "membership_id", nullable = false)
    private UUID membershipId;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "rotated_at")
    private Instant rotatedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected RefreshToken() {
    }

    public RefreshToken(UUID id, String tokenHash, UUID userId, UUID cabinetId, UUID membershipId,
                        Instant issuedAt, Instant expiresAt) {
        this.id = id;
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.cabinetId = cabinetId;
        this.membershipId = membershipId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.createdAt = issuedAt;
    }

    public UUID getId() { return id; }
    public String getTokenHash() { return tokenHash; }
    public UUID getUserId() { return userId; }
    public UUID getCabinetId() { return cabinetId; }
    public UUID getMembershipId() { return membershipId; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsable(Instant now) {
        return revokedAt == null && rotatedAt == null && expiresAt.isAfter(now);
    }
    public void rotate(UUID replacementId, Instant now) {
        rotatedAt = now;
        replacedBy = replacementId;
    }
    public void revoke(Instant now) { revokedAt = now; }
}