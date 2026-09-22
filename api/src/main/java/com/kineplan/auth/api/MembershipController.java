package com.kineplan.auth.api;

import com.kineplan.auth.application.MembershipService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/memberships")
public class MembershipController {
    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public List<MembershipResponse> list(Authentication authentication) {
        Claims claims = accessClaims(authentication);
        return membershipService.list(userId(authentication), cabinetId(claims));
    }

    @PostMapping("/invitations")
    public ResponseEntity<Void> invite(@Valid @RequestBody InviteMembershipRequest request,
                                       Authentication authentication) {
        Claims claims = accessClaims(authentication);
        membershipService.invite(userId(authentication), cabinetId(claims), request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/invitations/accept")
    public ResponseEntity<Void> accept(@RequestParam String token,
                                      @Valid @RequestBody AcceptInvitationRequest request) {
        membershipService.accept(token, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{membershipId}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID membershipId, Authentication authentication) {
        Claims claims = accessClaims(authentication);
        membershipService.deactivate(userId(authentication), cabinetId(claims), membershipId);
        return ResponseEntity.noContent().build();
    }

    private Claims accessClaims(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return claims;
    }

    private UUID userId(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UUID userId)) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return userId;
    }

    private UUID cabinetId(Claims claims) {
        return UUID.fromString(claims.get("cabinet_id", String.class));
    }
}