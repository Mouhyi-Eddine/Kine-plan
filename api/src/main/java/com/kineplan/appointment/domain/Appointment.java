package com.kineplan.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "appointments")
public class Appointment {
    @Id
    private UUID id;

    @TenantId
    @Column(name = "cabinet_id", nullable = false, updatable = false)
    private UUID cabinetId;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "practitioner_membership_id", nullable = false)
    private UUID practitionerMembershipId;

    @Column(name = "care_type_id")
    private UUID careTypeId;

    @Column(name = "series_id")
    private UUID seriesId;

    @Column(name = "occurrence_number")
    private Integer occurrenceNumber;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentSource source;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Appointment() {
    }

    public Appointment(UUID id, UUID cabinetId, UUID patientId, UUID practitionerMembershipId, UUID careTypeId,
                       Instant startAt, Instant endAt, AppointmentSource source, UUID createdBy) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.patientId = patientId;
        this.practitionerMembershipId = practitionerMembershipId;
        this.careTypeId = careTypeId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.source = source;
        this.status = AppointmentStatus.PLANIFIE;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getCabinetId() { return cabinetId; }
    public UUID getPatientId() { return patientId; }
    public UUID getPractitionerMembershipId() { return practitionerMembershipId; }
    public UUID getCareTypeId() { return careTypeId; }
    public UUID getSeriesId() { return seriesId; }
    public Integer getOccurrenceNumber() { return occurrenceNumber; }
    public Instant getStartAt() { return startAt; }
    public Instant getEndAt() { return endAt; }
    public AppointmentStatus getStatus() { return status; }
    public AppointmentSource getSource() { return source; }
    public String getCancellationReason() { return cancellationReason; }

    public void update(UUID patientId, UUID practitionerMembershipId, UUID careTypeId,
                       Instant startAt, Instant endAt, AppointmentSource source, UUID updatedBy) {
        this.patientId = patientId;
        this.practitionerMembershipId = practitionerMembershipId;
        this.careTypeId = careTypeId;
        this.startAt = startAt;
        this.endAt = endAt;
        this.source = source;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void cancel(String reason, UUID updatedBy) {
        this.status = AppointmentStatus.ANNULE;
        this.cancellationReason = reason;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public void attachToSeries(UUID seriesId, int occurrenceNumber) {
        this.seriesId = seriesId;
        this.occurrenceNumber = occurrenceNumber;
    }
}