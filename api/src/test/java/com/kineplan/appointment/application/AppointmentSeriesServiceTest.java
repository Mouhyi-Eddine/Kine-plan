package com.kineplan.appointment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kineplan.appointment.api.AppointmentResponse;
import com.kineplan.appointment.api.AppointmentSeriesRequest;
import com.kineplan.appointment.domain.AppointmentSeriesRepository;
import com.kineplan.appointment.domain.AppointmentSource;
import com.kineplan.appointment.domain.AppointmentStatus;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppointmentSeriesServiceTest {
    private final AppointmentSeriesRepository seriesRepository = mock(AppointmentSeriesRepository.class);
    private final AppointmentService appointmentService = mock(AppointmentService.class);
    private final AppointmentSeriesService service = new AppointmentSeriesService(seriesRepository, appointmentService);

    @Test
    void generatesOccurrencesAtTheRequestedInterval() {
        UUID cabinetId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();
        Instant firstStart = Instant.parse("2026-09-22T08:00:00Z");
        var request = new AppointmentSeriesRequest(patientId, practitionerId, null, firstStart,
                30, 3, 7, AppointmentSource.TELEPHONE);
        when(appointmentService.create(any(), any(), any(), any(), any())).thenAnswer(invocation ->
                new AppointmentResponse(UUID.randomUUID(), patientId, practitionerId, null,
                        invocation.<com.kineplan.appointment.api.AppointmentRequest>getArgument(2).startAt(),
                        invocation.<com.kineplan.appointment.api.AppointmentRequest>getArgument(2).endAt(),
                        AppointmentStatus.PLANIFIE, AppointmentSource.TELEPHONE, null));

        var response = service.create(cabinetId, actorId, request);

        assertThat(response.appointments()).hasSize(3);
        assertThat(response.appointments().get(0).startAt()).isEqualTo(firstStart);
        assertThat(response.appointments().get(1).startAt()).isEqualTo(firstStart.plusSeconds(7 * 86_400L));
        assertThat(response.appointments().get(2).startAt()).isEqualTo(firstStart.plusSeconds(14 * 86_400L));
        verify(seriesRepository).save(any());
    }
}