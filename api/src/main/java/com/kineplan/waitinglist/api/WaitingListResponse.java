package com.kineplan.waitinglist.api;

import com.kineplan.waitinglist.domain.WaitingListStatus;
import java.util.UUID;

public record WaitingListResponse(UUID id, UUID patientId, WaitingListStatus status) { }