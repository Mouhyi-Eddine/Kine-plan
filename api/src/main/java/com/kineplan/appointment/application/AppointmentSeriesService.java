package com.kineplan.appointment.application;

import com.kineplan.appointment.api.AppointmentRequest;
import com.kineplan.appointment.api.AppointmentSeriesRequest;
import com.kineplan.appointment.api.AppointmentSeriesResponse;
import com.kineplan.appointment.api.AppointmentResponse;
import com.kineplan.appointment.domain.AppointmentSeries;
import com.kineplan.appointment.domain.AppointmentSeriesRepository;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentSeriesService {
    private final AppointmentSeriesRepository seriesRepository;
    private final AppointmentService appointmentService;

    public AppointmentSeriesService(AppointmentSeriesRepository seriesRepository, AppointmentService appointmentService) {
        this.seriesRepository = seriesRepository;
        this.appointmentService = appointmentService;
    }

    @Transactional
    public AppointmentSeriesResponse create(UUID cabinetId, UUID actorId, AppointmentSeriesRequest request) {
        UUID seriesId = UUID.randomUUID();
        seriesRepository.save(new AppointmentSeries(seriesId, cabinetId, request.patientId(),
                request.practitionerMembershipId(), request.careTypeId(), request.occurrences(),
                request.intervalDays(), actorId));
        var appointments = new ArrayList<AppointmentResponse>();
        for (int occurrence = 0; occurrence < request.occurrences(); occurrence++) {
            var start = request.firstStartAt().plus(Duration.ofDays((long) occurrence * request.intervalDays()));
            appointments.add(appointmentService.create(cabinetId, actorId,
                    new AppointmentRequest(request.patientId(), request.practitionerMembershipId(), request.careTypeId(),
                            start, start.plus(Duration.ofMinutes(request.durationMinutes())), request.source()),
                    seriesId, occurrence + 1));
        }
        return new AppointmentSeriesResponse(seriesId, appointments);
    }
}