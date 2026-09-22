package com.kineplan.caretype.api;

import com.kineplan.caretype.application.CareTypeService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/care-types")
public class CareTypeController {
    private final CareTypeService service;

    public CareTypeController(CareTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<CareTypeResponse> list(Authentication authentication) {
        return service.list(cabinetId(authentication));
    }

    @PostMapping
    public ResponseEntity<CareTypeResponse> create(@Valid @RequestBody CareTypeRequest request,
                                                   Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(cabinetId(authentication), request));
    }

    private UUID cabinetId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return UUID.fromString(claims.get("cabinet_id", String.class));
    }
}