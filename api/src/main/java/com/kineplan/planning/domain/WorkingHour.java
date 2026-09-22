package com.kineplan.planning.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.TenantId;

@Entity
@Table(name = "working_hours")
public class WorkingHour {
    @Id private UUID id;
    @TenantId @Column(name = "cabinet_id", nullable = false, updatable = false) private UUID cabinetId;
    @Column(name = "practitioner_membership_id", nullable = false) private UUID practitionerMembershipId;
    @Column(name = "day_of_week", nullable = false) private int dayOfWeek;
    @Column(name = "start_time", nullable = false) private LocalTime startTime;
    @Column(name = "end_time", nullable = false) private LocalTime endTime;

    protected WorkingHour() { }

    public WorkingHour(UUID id, UUID cabinetId, UUID practitionerMembershipId, DayOfWeek dayOfWeek,
                       LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.cabinetId = cabinetId;
        this.practitionerMembershipId = practitionerMembershipId;
        this.dayOfWeek = dayOfWeek.getValue();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getPractitionerMembershipId() { return practitionerMembershipId; }
    public DayOfWeek getDayOfWeek() { return DayOfWeek.of(dayOfWeek); }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
}