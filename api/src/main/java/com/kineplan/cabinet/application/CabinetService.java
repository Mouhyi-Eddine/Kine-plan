package com.kineplan.cabinet.application;

import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.auth.domain.User;
import com.kineplan.auth.domain.UserRepository;
import com.kineplan.cabinet.api.CabinetResponse;
import com.kineplan.cabinet.api.OnboardingRequest;
import com.kineplan.cabinet.api.OnboardingResponse;
import com.kineplan.cabinet.api.UpdateCabinetRequest;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetStatus;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CabinetService {
    private static final String DEFAULT_PLAN = "ESSENTIAL";

    private final CabinetRepository cabinetRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CabinetService(CabinetRepository cabinetRepository, MembershipRepository membershipRepository,
                          UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.cabinetRepository = cabinetRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public OnboardingResponse onboard(OnboardingRequest request) {
        validateTimezone(request.timezone());
        String email = request.adminEmail().trim().toLowerCase();
        User user = userRepository.findByEmailIgnoreCase(email).map(existing -> {
            if (!passwordEncoder.matches(request.adminPassword(), existing.getPasswordHash())) {
                throw new CabinetException("Unable to complete onboarding");
            }
            return existing;
        }).orElseGet(() -> userRepository.save(new User(UUID.randomUUID(), email,
                passwordEncoder.encode(request.adminPassword()), request.adminFirstName().trim(),
                request.adminLastName().trim())));

        Instant now = Instant.now();
        Cabinet cabinet = cabinetRepository.save(new Cabinet(UUID.randomUUID(), request.cabinetName().trim(),
                request.address(), normalizeSiret(request.siret()), request.timezone().trim(), CabinetStatus.ACTIF,
                DEFAULT_PLAN, now));
        Membership membership = membershipRepository.save(new Membership(UUID.randomUUID(), user.getId(), cabinet.getId(),
                MembershipRole.ADMIN, MembershipStatus.ACTIVE, now));
        return new OnboardingResponse(cabinet.getId(), membership.getId());
    }

    @Transactional(readOnly = true)
    public CabinetResponse get(UUID userId, UUID cabinetId) {
        requireMembership(userId, cabinetId);
        return response(findCabinet(cabinetId));
    }

    @Transactional
    public CabinetResponse update(UUID userId, UUID cabinetId, UpdateCabinetRequest request) {
        Membership membership = requireMembership(userId, cabinetId);
        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new CabinetAccessDeniedException();
        }
        Cabinet cabinet = findCabinet(cabinetId);
        if (!cabinet.isActive()) {
            throw new CabinetException("Cabinet is not active");
        }
        validateTimezone(request.timezone());
        cabinet.updateProfile(request.name().trim(), request.address(), normalizeSiret(request.siret()), request.timezone().trim());
        return response(cabinetRepository.save(cabinet));
    }

    private Membership requireMembership(UUID userId, UUID cabinetId) {
        return membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(CabinetAccessDeniedException::new);
    }

    private Cabinet findCabinet(UUID cabinetId) {
        return cabinetRepository.findById(cabinetId)
            .orElseThrow(CabinetNotFoundException::new);
    }

    private CabinetResponse response(Cabinet cabinet) {
        return new CabinetResponse(cabinet.getId(), cabinet.getName(), cabinet.getAddress(), cabinet.getSiret(),
                cabinet.getTimezone(), cabinet.getStatus(), cabinet.getSubscriptionPlan(), cabinet.getCreatedAt());
    }

    private void validateTimezone(String timezone) {
        try {
            ZoneId.of(timezone.trim());
        } catch (RuntimeException exception) {
            throw new CabinetException("Invalid timezone");
        }
    }

    private String normalizeSiret(String siret) {
        if (siret == null || siret.isBlank()) {
            return null;
        }
        String normalized = siret.trim();
        if (!normalized.matches("\\d{14}")) {
            throw new CabinetException("Invalid SIRET");
        }
        return normalized;
    }
}
