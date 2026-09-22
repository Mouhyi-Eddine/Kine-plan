package com.kineplan.audit.api;

import com.kineplan.audit.application.AuditService;
import com.kineplan.auth.application.AuthenticationException;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {
    private final AuditService service;
    public AuditController(AuditService service) { this.service = service; }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditLogResponse> list(Pageable pageable, Authentication auth) {
        if (auth == null || !(auth.getDetails() instanceof Claims claims) || !"cabinet".equals(claims.get("token_type", String.class))) throw new AuthenticationException();
        return service.list(UUID.fromString(claims.get("cabinet_id", String.class)), pageable);
    }
}