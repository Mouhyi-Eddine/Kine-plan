package com.kineplan.auth.api;

import jakarta.validation.constraints.NotBlank;

public record AcceptInvitationRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        String password) {
}