package com.kineplan.waitinglist.api;

import com.kineplan.auth.application.AuthenticationException;
import com.kineplan.waitinglist.application.WaitingListService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/waiting-list")
public class WaitingListController {
    private final WaitingListService service;
    public WaitingListController(WaitingListService service) { this.service = service; }
    @GetMapping public List<WaitingListResponse> list(Authentication auth) { return service.list(cabinet(auth)); }
    @PostMapping public ResponseEntity<WaitingListResponse> register(@Valid @RequestBody WaitingListRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(cabinet(auth), request));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> remove(@PathVariable UUID id, Authentication auth) {
        service.remove(cabinet(auth), id); return ResponseEntity.noContent().build();
    }
    private UUID cabinet(Authentication auth) {
        if (auth == null || !(auth.getDetails() instanceof Claims claims) || !"cabinet".equals(claims.get("token_type", String.class))) throw new AuthenticationException();
        return UUID.fromString(claims.get("cabinet_id", String.class));
    }
}