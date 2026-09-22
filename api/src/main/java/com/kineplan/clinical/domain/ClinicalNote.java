package com.kineplan.clinical.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "clinical_notes")
public class ClinicalNote {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "patient_id", nullable = false) private UUID patientId;
    @Column(name = "appointment_id") private UUID appointmentId;
    @Column(name = "author_membership_id", nullable = false) private UUID authorMembershipId;
    @Column(nullable = false) private String content;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
    @Version @Column(nullable = false) private long version;

    protected ClinicalNote() { }
    public ClinicalNote(UUID id, UUID cabinetId, UUID patientId, UUID appointmentId,
                        UUID authorMembershipId, String content) {
        this.id = id; this.cabinetId = cabinetId; this.patientId = patientId; this.appointmentId = appointmentId;
        this.authorMembershipId = authorMembershipId; this.content = content; this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }
    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public UUID getAppointmentId() { return appointmentId; }
    public String getContent() { return content; }
    public void update(String content) { this.content = content; this.updatedAt = Instant.now(); }
}