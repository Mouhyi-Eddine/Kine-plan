package com.kineplan.appointment.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentSeriesRepository extends JpaRepository<AppointmentSeries, UUID> {
}