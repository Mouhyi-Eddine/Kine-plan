package com.kineplan.dashboard.application;

import com.kineplan.appointment.domain.AppointmentRepository;
import com.kineplan.appointment.domain.AppointmentStatus;
import com.kineplan.dashboard.api.DashboardResponse;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final AppointmentRepository repository;
    public DashboardService(AppointmentRepository repository) { this.repository = repository; }
    @Transactional(readOnly = true)
    public DashboardResponse appointments(UUID cabinetId, Instant from, Instant to) {
        long total = repository.countByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThan(cabinetId, from, to);
        long absences = repository.countByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThanAndStatus(
                cabinetId, from, to, AppointmentStatus.ABSENT);
        return new DashboardResponse(total, absences, total == 0 ? 0.0 : (double) absences / total);
    }
}