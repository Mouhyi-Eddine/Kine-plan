package com.kineplan.planning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "cabinet_closures")
public class CabinetClosure {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "start_at", nullable = false) private Instant startAt;
    @Column(name = "end_at", nullable = false) private Instant endAt;

    protected CabinetClosure() { }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
}