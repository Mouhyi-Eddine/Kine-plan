package com.kineplan.audit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "membership_id") private UUID membershipId;
    @Column(nullable = false) private String action;
    @Column(nullable = false) private String resource;
    @Column(name = "resource_id") private UUID resourceId;
    @Column(name = "occurred_at", nullable = false) private Instant occurredAt;

    protected AuditLog() { }
    public AuditLog(UUID cabinetId, UUID userId, UUID membershipId, String action, String resource, UUID resourceId) {
        this.id = UUID.randomUUID(); this.cabinetId = cabinetId; this.userId = userId; this.membershipId = membershipId;
        this.action = action; this.resource = resource; this.resourceId = resourceId; this.occurredAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public String getAction() { return action; }
    public String getResource() { return resource; }
    public UUID getResourceId() { return resourceId; }
    public Instant getOccurredAt() { return occurredAt; }
}