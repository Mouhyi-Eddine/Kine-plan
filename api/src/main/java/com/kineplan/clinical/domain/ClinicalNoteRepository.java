package com.kineplan.clinical.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
    List<ClinicalNote> findByCabinetIdAndPatientIdOrderByCreatedAtDesc(UUID cabinetId, UUID patientId);
    Optional<ClinicalNote> findByIdAndCabinetId(UUID id, UUID cabinetId);
}