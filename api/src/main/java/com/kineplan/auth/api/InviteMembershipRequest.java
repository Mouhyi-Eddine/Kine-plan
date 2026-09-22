package com.kineplan.auth.api;

import com.kineplan.auth.domain.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InviteMembershipRequest(@NotBlank @Email String email, @NotNull MembershipRole role) {
}