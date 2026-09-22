package com.kineplan.auth.api;

import com.kineplan.auth.domain.MembershipRole;
import java.util.UUID;

public record CabinetAccessResponse(UUID id, String name, MembershipRole role) {
}