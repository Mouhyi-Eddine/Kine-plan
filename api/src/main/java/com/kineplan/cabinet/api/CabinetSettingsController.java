package com.kineplan.cabinet.api;

import com.kineplan.auth.application.AuthenticationException;
import com.kineplan.cabinet.application.CabinetQuotaService;
import com.kineplan.cabinet.application.CabinetSettingsService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cabinet")
public class CabinetSettingsController {
    private final CabinetSettingsService settingsService;
    private final CabinetQuotaService quotaService;

    public CabinetSettingsController(CabinetSettingsService settingsService, CabinetQuotaService quotaService) {
        this.settingsService = settingsService;
        this.quotaService = quotaService;
    }

    @GetMapping("/settings")
    public CabinetSettingsResponse getSettings(Authentication authentication) {
        Claims claims = claims(authentication);
        return settingsService.get(userId(authentication), cabinetId(claims));
    }

    @PatchMapping("/settings")
    public CabinetSettingsResponse updateSettings(@Valid @RequestBody CabinetSettingsRequest request,
                                                  Authentication authentication) {
        Claims claims = claims(authentication);
        return settingsService.update(userId(authentication), cabinetId(claims), request);
    }

    @GetMapping("/quotas")
    public CabinetQuotasResponse getQuotas(Authentication authentication) {
        Claims claims = claims(authentication);
        return quotaService.get(userId(authentication), cabinetId(claims));
    }

    private Claims claims(Authentication authentication) {
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