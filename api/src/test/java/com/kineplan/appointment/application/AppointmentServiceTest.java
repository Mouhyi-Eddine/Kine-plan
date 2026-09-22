package com.kineplan.appointment.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kineplan.appointment.api.AppointmentRequest;
import com.kineplan.appointment.domain.AppointmentRepository;
import com.kineplan.appointment.domain.AppointmentSource;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.caretype.domain.CareTypeRepository;
import com.kineplan.patient.domain.PatientRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppointmentServiceTest {
    private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
    private final PatientRepository patientRepository = mock(PatientRepository.class);
    private final MembershipRepository membershipRepository = mock(MembershipRepository.class);
    private final CareTypeRepository careTypeRepository = mock(CareTypeRepository.class);
    private final AppointmentService service = new AppointmentService(appointmentRepository, patientRepository,
            membershipRepository, careTypeRepository);

    @Test
    void overlappingAppointmentIsRejectedBeforeSave() {
        UUID cabinetId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID practitionerId = UUID.randomUUID();
        Instant start = Instant.parse("2026-09-22T10:00:00Z");
        var request = new AppointmentRequest(patientId, practitionerId, null, start,
                start.plusSeconds(1800), AppointmentSource.TELEPHONE);
        when(patientRepository.findByIdAndCabinetId(patientId, cabinetId)).thenReturn(java.util.Optional.of(mock(com.kineplan.patient.domain.Patient.class)));
        when(membershipRepository.findById(practitionerId)).thenReturn(java.util.Optional.of(mock(com.kineplan.auth.domain.Membership.class)));
        when(membershipRepository.findById(practitionerId).get().isActive()).thenReturn(true);
        when(membershipRepository.findById(practitionerId).get().getCabinetId()).thenReturn(cabinetId);
        when(membershipRepository.findById(practitionerId).get().getRole()).thenReturn(com.kineplan.auth.domain.MembershipRole.KINESITHERAPEUTE);
        when(appointmentRepository.existsActiveOverlap(cabinetId, practitionerId, start, start.plusSeconds(1800), null))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(cabinetId, UUID.randomUUID(), request))
                .isInstanceOf(AppointmentException.class)
                .hasMessageContaining("overlapping");
        verifyNoInteractions(careTypeRepository);
    }
}