package com.kineplan.clinical.api;

import com.kineplan.auth.application.AuthenticationException;
import com.kineplan.clinical.application.ClinicalService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patients/{patientId}")
public class ClinicalController {
    private final ClinicalService service;

    public ClinicalController(ClinicalService service) { this.service = service; }

    @GetMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'KINESITHERAPEUTE')")
    public List<PrescriptionResponse> prescriptions(@PathVariable UUID patientId, Authentication authentication) {
        return service.prescriptions(cabinetId(authentication), patientId);
    }

    @PostMapping("/prescriptions")
    @PreAuthorize("hasAnyRole('ADMIN', 'KINESITHERAPEUTE')")
    public PrescriptionResponse prescription(@PathVariable UUID patientId, @Valid @RequestBody PrescriptionRequest request,
                                              Authentication authentication) {
        return service.createPrescription(cabinetId(authentication), patientId, request);
    }

    @GetMapping("/clinical-notes")
    @PreAuthorize("hasRole('KINESITHERAPEUTE')")
    public List<ClinicalNoteResponse> notes(@PathVariable UUID patientId, Authentication authentication) {
        return service.notes(cabinetId(authentication), patientId);
    }

    @PostMapping("/clinical-notes")
    @PreAuthorize("hasRole('KINESITHERAPEUTE')")
    public ClinicalNoteResponse note(@PathVariable UUID patientId, @Valid @RequestBody ClinicalNoteRequest request,
                                     Authentication authentication) {
        return service.createNote(cabinetId(authentication), patientId, membershipId(authentication), request);
    }

    @PatchMapping("/clinical-notes/{noteId}")
    @PreAuthorize("hasRole('KINESITHERAPEUTE')")
    public ClinicalNoteResponse update(@PathVariable UUID noteId, @Valid @RequestBody ClinicalNoteRequest request,
                                       Authentication authentication) {
        return service.updateNote(cabinetId(authentication), noteId, request);
    }

    private Claims claims(Authentication authentication) {
        if (authentication == null || !(authentication.getDetails() instanceof Claims claims)
                || !"cabinet".equals(claims.get("token_type", String.class))) {
            throw new AuthenticationException();
        }
        return claims;
    }
    private UUID cabinetId(Authentication authentication) { return UUID.fromString(claims(authentication).get("cabinet_id", String.class)); }
    private UUID membershipId(Authentication authentication) { return UUID.fromString(claims(authentication).get("membership_id", String.class)); }
}