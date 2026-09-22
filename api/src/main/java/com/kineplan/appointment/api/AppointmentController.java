package com.kineplan.appointment.api;

import com.kineplan.appointment.application.AppointmentService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AppointmentResponse> list(@RequestParam Instant from, @RequestParam Instant to,
                                          Pageable pageable, Authentication authentication) {
        return service.list(cabinetId(authentication), from, to, pageable);
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody AppointmentRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(cabinetId(authentication),
                userId(authentication), request));
    }

    @PatchMapping("/{appointmentId}")
    public AppointmentResponse update(@PathVariable UUID appointmentId, @Valid @RequestBody AppointmentRequest request,
                                      Authentication authentication) {
        return service.update(cabinetId(authentication), userId(authentication), appointmentId, request);
    }

    @PostMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID appointmentId,
                                       @RequestBody(required = false) CancelAppointmentRequest request,
                                       Authentication authentication) {
        service.cancel(cabinetId(authentication), userId(authentication), appointmentId,
                request == null ? new CancelAppointmentRequest(null) : request);
        return ResponseEntity.noContent().build();
    }

    private Claims claims(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return claims;
    }

    private UUID cabinetId(Authentication authentication) {
        return UUID.fromString(claims(authentication).get("cabinet_id", String.class));
    }

    private UUID userId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UUID userId)) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return userId;
    }
}