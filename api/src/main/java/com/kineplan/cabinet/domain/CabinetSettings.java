package com.kineplan.cabinet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.TenantId;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "cabinet_settings")
public class CabinetSettings {
    @Id
    @TenantId
    @Column(name = "cabinet_id", nullable = false)
    private UUID cabinetId;

    @Column(name = "reminder_delay_minutes", nullable = false)
    private int reminderDelayMinutes;

    @Column(name = "default_slot_duration_minutes", nullable = false)
    private int defaultSlotDurationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_texts", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> notificationTexts;

    protected CabinetSettings() {
    }

    public CabinetSettings(UUID cabinetId, int reminderDelayMinutes, int defaultSlotDurationMinutes,
                           Map<String, String> notificationTexts) {
        this.cabinetId = cabinetId;
        this.reminderDelayMinutes = reminderDelayMinutes;
        this.defaultSlotDurationMinutes = defaultSlotDurationMinutes;
        this.notificationTexts = notificationTexts == null ? new HashMap<>() : new HashMap<>(notificationTexts);
    }

    public UUID getCabinetId() { return cabinetId; }
    public int getReminderDelayMinutes() { return reminderDelayMinutes; }
    public int getDefaultSlotDurationMinutes() { return defaultSlotDurationMinutes; }
    public Map<String, String> getNotificationTexts() { return Map.copyOf(notificationTexts); }

    public void update(int reminderDelayMinutes, int defaultSlotDurationMinutes, Map<String, String> notificationTexts) {
        this.reminderDelayMinutes = reminderDelayMinutes;
        this.defaultSlotDurationMinutes = defaultSlotDurationMinutes;
        this.notificationTexts = notificationTexts == null ? new HashMap<>() : new HashMap<>(notificationTexts);
    }
}
