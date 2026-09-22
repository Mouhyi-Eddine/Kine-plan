package com.kineplan.patient.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    private UUID id;

    @TenantId
    @Column(name = "cabinet_id", nullable = false, updatable = false)
    private UUID cabinetId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private String phone;
    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "emergency_contact", columnDefinition = "jsonb")
    private Map<String, String> emergencyContact;

    @Column(name = "sms_consent", nullable = false)
    private boolean smsConsent;

    @Column(name = "email_consent", nullable = false)
    private boolean emailConsent;

    @Column(name = "rgpd_consent", nullable = false)
    private boolean rgpdConsent;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "anonymized_at")
    private Instant anonymizedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Patient() {
    }

    public Patient(UUID id, UUID cabinetId, String firstName, String lastName, LocalDate birthDate,
                   String phone, String email, Map<String, String> emergencyContact,
                   boolean smsConsent, boolean emailConsent, boolean rgpdConsent) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.emergencyContact = emergencyContact;
        this.smsConsent = smsConsent;
        this.emailConsent = emailConsent;
        this.rgpdConsent = rgpdConsent;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public UUID getId() { return id; }
    public UUID getCabinetId() { return cabinetId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public Map<String, String> getEmergencyContact() { return emergencyContact; }
    public boolean isSmsConsent() { return smsConsent; }
    public boolean isEmailConsent() { return emailConsent; }
    public boolean isRgpdConsent() { return rgpdConsent; }
    public Instant getArchivedAt() { return archivedAt; }
    public Instant getAnonymizedAt() { return anonymizedAt; }

    public void update(String firstName, String lastName, LocalDate birthDate, String phone, String email,
                       Map<String, String> emergencyContact, boolean smsConsent, boolean emailConsent,
                       boolean rgpdConsent) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.phone = phone;
        this.email = email;
        this.emergencyContact = emergencyContact;
        this.smsConsent = smsConsent;
        this.emailConsent = emailConsent;
        this.rgpdConsent = rgpdConsent;
        this.updatedAt = Instant.now();
    }

    public void archive() {
        archivedAt = Instant.now();
        updatedAt = archivedAt;
    }

    public void anonymize() {
        firstName = "Patient";
        lastName = "Anonymise";
        birthDate = null;
        phone = null;
        email = null;
        emergencyContact = null;
        smsConsent = false;
        emailConsent = false;
        rgpdConsent = false;
        anonymizedAt = Instant.now();
        updatedAt = anonymizedAt;
    }
}