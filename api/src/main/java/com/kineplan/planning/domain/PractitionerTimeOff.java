package com.kineplan.planning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "practitioner_time_off")
public class PractitionerTimeOff {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "practitioner_membership_id", nullable = false) private UUID practitionerMembershipId;
    @Column(name = "start_at", nullable = false) private Instant startAt;
    @Column(name = "end_at", nullable = false) private Instant endAt;

    protected PractitionerTimeOff() { }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
}