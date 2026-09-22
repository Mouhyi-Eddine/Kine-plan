package com.kineplan.cabinet.api;

import com.kineplan.auth.application.AuthenticationException;
import com.kineplan.cabinet.application.CabinetService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cabinet")
public class CabinetController {
    private final CabinetService service;

    public CabinetController(CabinetService service) {
        this.service = service;
    }

    @GetMapping
    public CabinetResponse get(Authentication authentication) {
        Claims claims = cabinetClaims(authentication);
        return service.get(userId(authentication), cabinetId(claims));
    }

    @PatchMapping
    public CabinetResponse update(@Valid @RequestBody UpdateCabinetRequest request,
                                  Authentication authentication) {
        Claims claims = cabinetClaims(authentication);
        return service.update(userId(authentication), cabinetId(claims), request);
    }

    private Claims cabinetClaims(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new AuthenticationException();
        }
        return claims;
    }

    private UUID userId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UUID userId)) {
            throw new AuthenticationException();
        }
        return userId;
    }

    private UUID cabinetId(Claims claims) {
        return UUID.fromString(claims.get("cabinet_id", String.class));
    }
}