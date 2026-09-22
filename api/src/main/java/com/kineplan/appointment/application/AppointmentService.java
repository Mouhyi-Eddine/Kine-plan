package com.kineplan.appointment.application;

import com.kineplan.appointment.api.AppointmentRequest;
import com.kineplan.appointment.api.AppointmentResponse;
import com.kineplan.appointment.api.CancelAppointmentRequest;
import com.kineplan.appointment.domain.Appointment;
import com.kineplan.appointment.domain.AppointmentRepository;
import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.patient.domain.Patient;
import com.kineplan.patient.domain.PatientRepository;
import com.kineplan.caretype.domain.CareTypeRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final MembershipRepository membershipRepository;
    private final CareTypeRepository careTypeRepository;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientRepository patientRepository,
                              MembershipRepository membershipRepository, CareTypeRepository careTypeRepository) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.membershipRepository = membershipRepository;
        this.careTypeRepository = careTypeRepository;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> list(UUID cabinetId, Instant from, Instant to, Pageable pageable) {
        return appointmentRepository.findByCabinetIdAndStartAtGreaterThanEqualAndStartAtLessThan(cabinetId, from, to,
                pageable).map(this::toResponse);
    }

    @Transactional
    public AppointmentResponse create(UUID cabinetId, UUID actorId, AppointmentRequest request) {
        return create(cabinetId, actorId, request, null, null);
    }

    @Transactional
    public AppointmentResponse create(UUID cabinetId, UUID actorId, AppointmentRequest request,
                                      UUID seriesId, Integer occurrenceNumber) {
        validateReferences(cabinetId, request);
        ensureNoOverlap(cabinetId, request, null);
        Appointment appointment = new Appointment(UUID.randomUUID(), cabinetId, request.patientId(),
                request.practitionerMembershipId(), request.careTypeId(), request.startAt(), request.endAt(),
                request.source(), actorId);
        if (seriesId != null && occurrenceNumber != null) {
            appointment.attachToSeries(seriesId, occurrenceNumber);
        }
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse update(UUID cabinetId, UUID actorId, UUID appointmentId, AppointmentRequest request) {
        Appointment appointment = find(cabinetId, appointmentId);
        validateReferences(cabinetId, request);
        ensureNoOverlap(cabinetId, request, appointmentId);
        appointment.update(request.patientId(), request.practitionerMembershipId(), request.careTypeId(),
                request.startAt(), request.endAt(), request.source(), actorId);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public void cancel(UUID cabinetId, UUID actorId, UUID appointmentId, CancelAppointmentRequest request) {
        Appointment appointment = find(cabinetId, appointmentId);
        appointment.cancel(request.reason(), actorId);
        appointmentRepository.save(appointment);
    }

    private void validateReferences(UUID cabinetId, AppointmentRequest request) {
        if (!request.endAt().isAfter(request.startAt())) {
            throw new AppointmentException("Appointment end must be after start");
        }
        Patient patient = patientRepository.findByIdAndCabinetId(request.patientId(), cabinetId)
                .filter(candidate -> candidate.getArchivedAt() == null)
                .orElseThrow(() -> new AppointmentException("Patient not found"));
        Membership membership = membershipRepository.findById(request.practitionerMembershipId())
                .filter(candidate -> candidate.isActive() && candidate.getCabinetId().equals(cabinetId)
                        && candidate.getRole() == MembershipRole.KINESITHERAPEUTE)
                .orElseThrow(() -> new AppointmentException("Practitioner not found"));
        if (request.careTypeId() != null) {
            careTypeRepository.findByIdAndCabinetId(request.careTypeId(), cabinetId)
                    .filter(type -> type.isActive())
                    .orElseThrow(() -> new AppointmentException("Care type not found"));
        }
    }

    private void ensureNoOverlap(UUID cabinetId, AppointmentRequest request, UUID appointmentId) {
        if (appointmentRepository.existsActiveOverlap(cabinetId, request.practitionerMembershipId(),
                request.startAt(), request.endAt(), appointmentId)) {
            throw new AppointmentException("Practitioner already has an overlapping appointment");
        }
    }

    private Appointment find(UUID cabinetId, UUID appointmentId) {
        return appointmentRepository.findByIdAndCabinetId(appointmentId, cabinetId)
                .orElseThrow(AppointmentNotFoundException::new);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(appointment.getId(), appointment.getPatientId(),
                appointment.getPractitionerMembershipId(), appointment.getCareTypeId(), appointment.getStartAt(),
                appointment.getEndAt(), appointment.getStatus(), appointment.getSource(), appointment.getCancellationReason());
    }
}