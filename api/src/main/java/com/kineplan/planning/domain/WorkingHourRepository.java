package com.kineplan.planning.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkingHourRepository extends JpaRepository<WorkingHour, UUID> {
    List<WorkingHour> findByCabinetIdAndPractitionerMembershipId(UUID cabinetId, UUID practitionerMembershipId);
}