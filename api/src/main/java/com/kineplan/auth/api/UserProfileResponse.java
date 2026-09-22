package com.kineplan.auth.api;

import com.kineplan.auth.domain.MembershipRole;
import java.util.UUID;

public record UserProfileResponse(
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phone,
        UUID cabinetId,
        String cabinetName,
        UUID membershipId,
        MembershipRole role) {
}