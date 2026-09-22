package com.kineplan.auth.api;

import com.kineplan.auth.application.AuthenticationService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationService.login(request.email(), request.password());
    }

    @PostMapping("/select-cabinet")
    public TokenResponse selectCabinet(@Valid @RequestBody SelectCabinetRequest request,
                                       Authentication authentication) {
        return authenticationService.selectCabinet(userId(authentication), request.cabinetId());
    }

    @PostMapping("/switch-cabinet")
    public TokenResponse switchCabinet(@Valid @RequestBody SelectCabinetRequest request,
                                       Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return authenticationService.selectCabinet(userId, request.cabinetId());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                               Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        authenticationService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    private UUID userId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)
                || !(authentication.getDetails() instanceof Claims claims)
                || !"pre-auth".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return userId;
    }
}