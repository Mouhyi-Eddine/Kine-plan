package com.kineplan.waitinglist.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, UUID> {
    List<WaitingListEntry> findByCabinetIdAndStatusOrderByCreatedAtAsc(UUID cabinetId, WaitingListStatus status);
    java.util.Optional<WaitingListEntry> findByIdAndCabinetId(UUID id, UUID cabinetId);
}