package com.kineplan.patient.api;

import com.kineplan.patient.application.PatientService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public Page<PatientResponse> search(@RequestParam(required = false) String query,
                                       Pageable pageable, Authentication authentication) {
        return patientService.search(cabinetId(authentication), query, pageable);
    }

    @PostMapping
    public ResponseEntity<PatientResponse> create(@Valid @RequestBody CreatePatientRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.create(cabinetId(authentication), request));
    }

    @GetMapping("/{patientId}")
    public PatientResponse get(@PathVariable UUID patientId, Authentication authentication) {
        return patientService.get(cabinetId(authentication), patientId);
    }

    @PatchMapping("/{patientId}")
    public PatientResponse update(@PathVariable UUID patientId, @Valid @RequestBody UpdatePatientRequest request,
                                  Authentication authentication) {
        return patientService.update(cabinetId(authentication), patientId, request);
    }

    @PostMapping("/{patientId}/archive")
    @PreAuthorize("hasAnyRole('ADMIN', 'KINESITHERAPEUTE')")
    public ResponseEntity<Void> archive(@PathVariable UUID patientId, Authentication authentication) {
        patientService.archive(cabinetId(authentication), patientId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{patientId}/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'KINESITHERAPEUTE')")
    public PatientResponse export(@PathVariable UUID patientId, Authentication authentication) {
        return patientService.get(cabinetId(authentication), patientId);
    }

    @PostMapping("/{patientId}/anonymize")
    @PreAuthorize("hasRole('ADMIN')")
    public PatientResponse anonymize(@PathVariable UUID patientId, Authentication authentication) {
        return patientService.anonymize(cabinetId(authentication), patientId);
    }

    private UUID cabinetId(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new com.kineplan.auth.application.AuthenticationException();
        }
        return UUID.fromString(claims.get("cabinet_id", String.class));
    }
}