package com.kineplan.waitinglist.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record WaitingListRequest(@NotNull UUID patientId, UUID careTypeId, UUID practitionerMembershipId) { }