package com.kineplan.appointment.api;

import com.kineplan.appointment.application.AppointmentSeriesService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointment-series")
public class AppointmentSeriesController {
    private final AppointmentSeriesService service;

    public AppointmentSeriesController(AppointmentSeriesService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<AppointmentSeriesResponse> create(@Valid @RequestBody AppointmentSeriesRequest request,
                                                             Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID actorId)
                || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        UUID cabinetId = UUID.fromString(claims.get("cabinet_id", String.class));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(cabinetId, actorId, request));
    }
}