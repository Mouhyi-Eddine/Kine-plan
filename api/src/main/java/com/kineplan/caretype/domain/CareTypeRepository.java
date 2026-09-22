package com.kineplan.caretype.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareTypeRepository extends JpaRepository<CareType, UUID> {
    List<CareType> findByCabinetIdAndActiveTrueOrderByName(UUID cabinetId);
    Optional<CareType> findByIdAndCabinetId(UUID id, UUID cabinetId);
}