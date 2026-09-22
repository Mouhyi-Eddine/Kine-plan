package com.kineplan.auth.api;

import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import java.util.UUID;

public record MembershipResponse(
        UUID id,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        MembershipRole role,
        MembershipStatus status) {
}