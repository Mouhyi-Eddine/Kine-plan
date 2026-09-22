package com.kineplan.waitinglist.application;

import com.kineplan.waitinglist.api.WaitingListRequest;
import com.kineplan.waitinglist.api.WaitingListResponse;
import com.kineplan.waitinglist.domain.WaitingListEntry;
import com.kineplan.waitinglist.domain.WaitingListRepository;
import com.kineplan.waitinglist.domain.WaitingListStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WaitingListService {
    private final WaitingListRepository repository;
    public WaitingListService(WaitingListRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<WaitingListResponse> list(UUID cabinetId) {
        return repository.findByCabinetIdAndStatusOrderByCreatedAtAsc(cabinetId, WaitingListStatus.ACTIVE)
                .stream().map(this::response).toList();
    }

    @Transactional
    public WaitingListResponse register(UUID cabinetId, WaitingListRequest request) {
        return response(repository.save(new WaitingListEntry(UUID.randomUUID(), cabinetId, request.patientId(),
                request.careTypeId(), request.practitionerMembershipId())));
    }

    @Transactional
    public void remove(UUID cabinetId, UUID entryId) {
        WaitingListEntry entry = repository.findByIdAndCabinetId(entryId, cabinetId)
                .orElseThrow(() -> new IllegalArgumentException("Waiting list entry not found"));
        repository.delete(entry);
    }

    private WaitingListResponse response(WaitingListEntry entry) {
        return new WaitingListResponse(entry.getId(), entry.getPatientId(), entry.getStatus());
    }
}