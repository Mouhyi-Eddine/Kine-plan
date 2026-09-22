package com.kineplan.patient.domain;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Page<Patient> findByCabinetIdAndArchivedAtIsNull(UUID cabinetId, Pageable pageable);
    Page<Patient> findByCabinetIdAndArchivedAtIsNullAndFirstNameContainingIgnoreCaseOrCabinetIdAndArchivedAtIsNullAndLastNameContainingIgnoreCase(
            UUID firstCabinetId, String firstName, UUID secondCabinetId, String lastName, Pageable pageable);
    java.util.Optional<Patient> findByIdAndCabinetId(UUID id, UUID cabinetId);
}