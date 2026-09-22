package com.kineplan.planning.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CabinetClosureRepository extends JpaRepository<CabinetClosure, UUID> {
    List<CabinetClosure> findByCabinetIdAndStartAtLessThanAndEndAtGreaterThan(UUID cabinetId, Instant to, Instant from);
}