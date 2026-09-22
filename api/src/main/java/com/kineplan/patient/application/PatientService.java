package com.kineplan.patient.application;

import com.kineplan.patient.api.CreatePatientRequest;
import com.kineplan.patient.api.PatientResponse;
import com.kineplan.patient.api.UpdatePatientRequest;
import com.kineplan.patient.domain.Patient;
import com.kineplan.patient.domain.PatientRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientService {
    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> search(UUID cabinetId, String query, Pageable pageable) {
        Page<Patient> patients;
        if (query == null || query.isBlank()) {
            patients = patientRepository.findByCabinetIdAndArchivedAtIsNull(cabinetId, pageable);
        } else {
            patients = patientRepository
                    .findByCabinetIdAndArchivedAtIsNullAndFirstNameContainingIgnoreCaseOrCabinetIdAndArchivedAtIsNullAndLastNameContainingIgnoreCase(
                            cabinetId, query.trim(), cabinetId, query.trim(), pageable);
        }
        return patients.map(this::toResponse);
    }

    @Transactional
    public PatientResponse create(UUID cabinetId, CreatePatientRequest request) {
        Patient patient = new Patient(UUID.randomUUID(), cabinetId, request.firstName(), request.lastName(),
                request.birthDate(), request.phone(), request.email(), request.emergencyContact(),
                request.smsConsent(), request.emailConsent(), request.rgpdConsent());
        return toResponse(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public PatientResponse get(UUID cabinetId, UUID patientId) {
        return toResponse(find(cabinetId, patientId));
    }

    @Transactional
    public PatientResponse update(UUID cabinetId, UUID patientId, UpdatePatientRequest request) {
        Patient patient = find(cabinetId, patientId);
        patient.update(request.firstName(), request.lastName(), request.birthDate(), request.phone(), request.email(),
                request.emergencyContact(), request.smsConsent(), request.emailConsent(), request.rgpdConsent());
        return toResponse(patientRepository.save(patient));
    }

    @Transactional
    public void archive(UUID cabinetId, UUID patientId) {
        Patient patient = find(cabinetId, patientId);
        patient.archive();
        patientRepository.save(patient);
    }

    @Transactional
    public PatientResponse anonymize(UUID cabinetId, UUID patientId) {
        Patient patient = find(cabinetId, patientId);
        patient.anonymize();
        return toResponse(patientRepository.save(patient));
    }

    private Patient find(UUID cabinetId, UUID patientId) {
        return patientRepository.findByIdAndCabinetId(patientId, cabinetId)
                .orElseThrow(() -> new PatientNotFoundException());
    }

    private PatientResponse toResponse(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getFirstName(), patient.getLastName(),
                patient.getBirthDate(), patient.getPhone(), patient.getEmail(), patient.getEmergencyContact(),
                patient.isSmsConsent(), patient.isEmailConsent(), patient.isRgpdConsent(), patient.getArchivedAt());
    }
}