package com.kineplan.waitinglist.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "waiting_list_entries")
public class WaitingListEntry {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "patient_id", nullable = false) private UUID patientId;
    @Column(name = "care_type_id") private UUID careTypeId;
    @Column(name = "practitioner_membership_id") private UUID practitionerMembershipId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private WaitingListStatus status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "proposed_appointment_id") private UUID proposedAppointmentId;

    protected WaitingListEntry() { }
    public WaitingListEntry(UUID id, UUID cabinetId, UUID patientId, UUID careTypeId, UUID practitionerMembershipId) {
        this.id = id; this.cabinetId = cabinetId; this.patientId = patientId; this.careTypeId = careTypeId;
        this.practitionerMembershipId = practitionerMembershipId; this.status = WaitingListStatus.ACTIVE;
        this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public WaitingListStatus getStatus() { return status; }
    public void propose(UUID appointmentId) { status = WaitingListStatus.PROPOSED; proposedAppointmentId = appointmentId; }
}