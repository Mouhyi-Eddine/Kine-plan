package com.kineplan.clinical.application;

import com.kineplan.clinical.api.ClinicalNoteRequest;
import com.kineplan.clinical.api.ClinicalNoteResponse;
import com.kineplan.clinical.api.PrescriptionRequest;
import com.kineplan.clinical.api.PrescriptionResponse;
import com.kineplan.clinical.domain.ClinicalNote;
import com.kineplan.clinical.domain.ClinicalNoteRepository;
import com.kineplan.clinical.domain.Prescription;
import com.kineplan.clinical.domain.PrescriptionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClinicalService {
    private final PrescriptionRepository prescriptionRepository;
    private final ClinicalNoteRepository noteRepository;

    public ClinicalService(PrescriptionRepository prescriptionRepository, ClinicalNoteRepository noteRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public PrescriptionResponse createPrescription(UUID cabinetId, UUID patientId, PrescriptionRequest request) {
        return prescriptionResponse(prescriptionRepository.save(new Prescription(UUID.randomUUID(), cabinetId, patientId,
                request.prescriber(), request.prescribedAt(), request.sessionsPrescribed(), request.expiresAt())));
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> prescriptions(UUID cabinetId, UUID patientId) {
        return prescriptionRepository.findByCabinetIdAndPatientId(cabinetId, patientId).stream()
                .map(this::prescriptionResponse).toList();
    }

    @Transactional
    public ClinicalNoteResponse createNote(UUID cabinetId, UUID patientId, UUID membershipId, ClinicalNoteRequest request) {
        return response(noteRepository.save(new ClinicalNote(UUID.randomUUID(), cabinetId, patientId,
                request.appointmentId(), membershipId, request.content())));
    }

    @Transactional(readOnly = true)
    public List<ClinicalNoteResponse> notes(UUID cabinetId, UUID patientId) {
        return noteRepository.findByCabinetIdAndPatientIdOrderByCreatedAtDesc(cabinetId, patientId)
                .stream().map(this::response).toList();
    }

    @Transactional
    public ClinicalNoteResponse updateNote(UUID cabinetId, UUID noteId, ClinicalNoteRequest request) {
        ClinicalNote note = noteRepository.findByIdAndCabinetId(noteId, cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Clinical note not found"));
        note.update(request.content());
        return response(noteRepository.save(note));
    }

    private ClinicalNoteResponse response(ClinicalNote note) {
        return new ClinicalNoteResponse(note.getId(), note.getPatientId(), note.getAppointmentId(), note.getContent());
    }

    private PrescriptionResponse prescriptionResponse(Prescription prescription) {
        return new PrescriptionResponse(prescription.getId(), prescription.getPatientId(), prescription.getPrescriber(),
                prescription.getPrescribedAt(), prescription.getSessionsPrescribed(), prescription.getSessionsConsumed(),
                prescription.getExpiresAt());
    }
}