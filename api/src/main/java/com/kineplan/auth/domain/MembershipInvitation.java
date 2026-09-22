package com.kineplan.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership_invitations")
public class MembershipInvitation {
    @Id
    private UUID id;

    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Column(nullable = false, length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected MembershipInvitation() {
    }

    public MembershipInvitation(UUID id, UUID cabinetId, String email, MembershipRole role,
                                String tokenHash, Instant expiresAt, Instant createdAt) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.email = email;
        this.role = role;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getCabinetId() { return cabinetId; }
    public String getEmail() { return email; }
    public MembershipRole getRole() { return role; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isUsable(Instant now) { return acceptedAt == null && expiresAt.isAfter(now); }
    public void accept(Instant now) { acceptedAt = now; }
}