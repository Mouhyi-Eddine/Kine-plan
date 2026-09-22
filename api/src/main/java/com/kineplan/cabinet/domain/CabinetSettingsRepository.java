package com.kineplan.cabinet.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetSettingsRepository extends JpaRepository<CabinetSettings, UUID> {
    Optional<CabinetSettings> findByCabinetId(UUID cabinetId);
}
