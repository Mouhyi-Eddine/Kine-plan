package com.kineplan.caretype.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "care_types")
public class CareType {
    @Id
    private UUID id;

    @TenantId
    @Column(name = "cabinet_id", nullable = false, updatable = false)
    private UUID cabinetId;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_duration_minutes", nullable = false)
    private int defaultDurationMinutes;

    @Column(name = "display_color")
    private String displayColor;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CareType() {
    }

    public CareType(UUID id, UUID cabinetId, String name, int defaultDurationMinutes, String displayColor) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.name = name;
        this.defaultDurationMinutes = defaultDurationMinutes;
        this.displayColor = displayColor;
        this.active = true;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getCabinetId() { return cabinetId; }
    public String getName() { return name; }
    public int getDefaultDurationMinutes() { return defaultDurationMinutes; }
    public String getDisplayColor() { return displayColor; }
    public boolean isActive() { return active; }
}