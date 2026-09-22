package com.kineplan.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "appointment_series")
public class AppointmentSeries {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "patient_id", nullable = false) private UUID patientId;
    @Column(name = "practitioner_membership_id", nullable = false) private UUID practitionerMembershipId;
    @Column(name = "care_type_id") private UUID careTypeId;
    @Column(nullable = false) private int occurrences;
    @Column(name = "interval_days", nullable = false) private int intervalDays;
    @Column(name = "created_by", nullable = false) private UUID createdBy;
    @Column(name = "created_at", nullable = false) private Instant createdAt;

    protected AppointmentSeries() { }

    public AppointmentSeries(UUID id, UUID cabinetId, UUID patientId, UUID practitionerMembershipId,
                             UUID careTypeId, int occurrences, int intervalDays, UUID createdBy) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.patientId = patientId;
        this.practitionerMembershipId = practitionerMembershipId;
        this.careTypeId = careTypeId;
        this.occurrences = occurrences;
        this.intervalDays = intervalDays;
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
}