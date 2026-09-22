package com.kineplan.notification.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "notification_deliveries")
public class NotificationDelivery {
    @Id
    private UUID id;

    @TenantId
    @Column(name = "cabinet_id", nullable = false, updatable = false)
    private UUID cabinetId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "patient_id")
    private UUID patientId;

    @Column(nullable = false)
    private String channel;

    @Column(nullable = false)
    private String status;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected NotificationDelivery() {
    }

    public NotificationDelivery(UUID id, UUID cabinetId, UUID appointmentId, UUID patientId,
                                String channel, String status, Instant sentAt, Instant createdAt) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.channel = channel;
        this.status = status;
        this.sentAt = sentAt;
        this.createdAt = createdAt;
    }
}
