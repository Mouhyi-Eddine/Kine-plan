package com.kineplan.planning.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerTimeOffRepository extends JpaRepository<PractitionerTimeOff, UUID> {
    List<PractitionerTimeOff> findByCabinetIdAndPractitionerMembershipIdAndStartAtLessThanAndEndAtGreaterThan(
            UUID cabinetId, UUID practitionerMembershipId, Instant to, Instant from);
}