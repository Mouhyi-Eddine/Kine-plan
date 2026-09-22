package com.kineplan.planning.api;

import io.jsonwebtoken.Claims;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.kineplan.planning.application.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/availability")
public class AvailabilityController {
    private final AvailabilityService service;

    public AvailabilityController(AvailabilityService service) { this.service = service; }

    @GetMapping
    public List<AvailabilityResponse> find(
            @RequestParam UUID practitionerMembershipId,
            @RequestParam UUID careTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        UUID cabinetId = UUID.fromString(claims.get("cabinet_id", String.class));
        return service.find(cabinetId, practitionerMembershipId, careTypeId, from, to);
    }
}