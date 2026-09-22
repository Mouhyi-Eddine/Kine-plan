package com.kineplan.cabinet.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/onboarding")
public class OnboardingController {
    private final com.kineplan.cabinet.application.CabinetService service;

    public OnboardingController(com.kineplan.cabinet.application.CabinetService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<OnboardingResponse> create(@Valid @RequestBody OnboardingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.onboard(request));
    }
}