package com.kineplan.auth.api;

import com.kineplan.auth.application.AuthenticationService;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final AuthenticationService authenticationService;

    public MeController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public UserProfileResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)
                || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return authenticationService.me(userId,
                UUID.fromString(claims.get("cabinet_id", String.class)),
                UUID.fromString(claims.get("membership_id", String.class)));
    }
}