package com.kineplan.clinical.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "prescriptions")
public class Prescription {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "patient_id", nullable = false) private UUID patientId;
    @Column(nullable = false) private String prescriber;
    @Column(name = "prescribed_at", nullable = false) private LocalDate prescribedAt;
    @Column(name = "sessions_prescribed", nullable = false) private int sessionsPrescribed;
    @Column(name = "sessions_consumed", nullable = false) private int sessionsConsumed;
    @Column(name = "expires_at") private LocalDate expiresAt;

    protected Prescription() { }
    public Prescription(UUID id, UUID cabinetId, UUID patientId, String prescriber, LocalDate prescribedAt,
                        int sessionsPrescribed, LocalDate expiresAt) {
        this.id = id; this.cabinetId = cabinetId; this.patientId = patientId; this.prescriber = prescriber;
        this.prescribedAt = prescribedAt; this.sessionsPrescribed = sessionsPrescribed; this.expiresAt = expiresAt;
    }
    public UUID getId() { return id; }
    public UUID getPatientId() { return patientId; }
    public String getPrescriber() { return prescriber; }
    public LocalDate getPrescribedAt() { return prescribedAt; }
    public int getSessionsPrescribed() { return sessionsPrescribed; }
    public int getSessionsConsumed() { return sessionsConsumed; }
    public LocalDate getExpiresAt() { return expiresAt; }
    public void consume() { sessionsConsumed++; }
}