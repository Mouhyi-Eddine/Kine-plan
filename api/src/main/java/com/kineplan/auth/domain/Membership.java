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
@Table(name = "memberships")
public class Membership {
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(name = "invited_at")
    private Instant invitedAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Membership() {
    }

    public Membership(UUID id, UUID userId, UUID cabinetId, MembershipRole role,
                      MembershipStatus status, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.cabinetId = cabinetId;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getCabinetId() { return cabinetId; }
    public MembershipRole getRole() { return role; }
    public MembershipStatus getStatus() { return status; }

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    public void deactivate() {
        status = MembershipStatus.DESACTIVE;
        updatedAt = Instant.now();
    }
}