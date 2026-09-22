package com.kineplan.dashboard.api;

import com.kineplan.auth.application.AuthenticationException;
import com.kineplan.dashboard.application.DashboardService;
import io.jsonwebtoken.Claims;
import java.time.Instant;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;
    public DashboardController(DashboardService service) { this.service = service; }
    @GetMapping("/appointments")
    public DashboardResponse appointments(@RequestParam Instant from, @RequestParam Instant to, Authentication auth) {
        if (auth == null || !(auth.getDetails() instanceof Claims claims) || !"cabinet".equals(claims.get("token_type", String.class))) throw new AuthenticationException();
        return service.appointments(UUID.fromString(claims.get("cabinet_id", String.class)), from, to);
    }
}