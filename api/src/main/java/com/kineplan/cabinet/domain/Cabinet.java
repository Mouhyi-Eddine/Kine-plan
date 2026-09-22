package com.kineplan.cabinet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "cabinets")
public class Cabinet {
    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    private String address;
    private String siret;

    @Column(nullable = false)
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CabinetStatus status;

    @Column(name = "subscription_plan", nullable = false)
    private String subscriptionPlan;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Cabinet() {
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getTimezone() { return timezone; }
    public CabinetStatus getStatus() { return status; }

    public boolean isActive() {
        return status == CabinetStatus.ACTIF;
    }
}