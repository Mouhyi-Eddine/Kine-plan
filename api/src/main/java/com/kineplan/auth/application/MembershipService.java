package com.kineplan.auth.application;

import com.kineplan.auth.api.AcceptInvitationRequest;
import com.kineplan.auth.api.InviteMembershipRequest;
import com.kineplan.auth.api.MembershipResponse;
import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipInvitation;
import com.kineplan.auth.domain.MembershipInvitationRepository;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.auth.domain.User;
import com.kineplan.auth.domain.UserRepository;
import com.kineplan.auth.domain.UserStatus;
import com.kineplan.cabinet.application.CabinetQuotaService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MembershipService {
    private static final Duration INVITATION_TTL = Duration.ofDays(7);
    private final MembershipRepository membershipRepository;
    private final MembershipInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CabinetQuotaService quotaService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Clock clock = Clock.systemUTC();

    public MembershipService(MembershipRepository membershipRepository,
                              MembershipInvitationRepository invitationRepository,
                              UserRepository userRepository, PasswordEncoder passwordEncoder,
                              CabinetQuotaService quotaService) {
        this.membershipRepository = membershipRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.quotaService = quotaService;
    }

    @Transactional(readOnly = true)
    public List<MembershipResponse> list(UUID actorId, UUID cabinetId) {
        requireAdmin(actorId, cabinetId);
        return membershipRepository.findByCabinetId(cabinetId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void invite(UUID actorId, UUID cabinetId, InviteMembershipRequest request) {
        requireAdmin(actorId, cabinetId);
        String email = request.email().trim().toLowerCase();
        String rawToken = randomToken();
        Instant now = clock.instant();
        invitationRepository.save(new MembershipInvitation(UUID.randomUUID(), cabinetId, email, request.role(),
                hash(rawToken), now.plus(INVITATION_TTL), now));
        // The notification port will deliver rawToken when the notification module is added.
    }

    @Transactional
    public void accept(String rawToken, AcceptInvitationRequest request) {
        MembershipInvitation invitation = invitationRepository.findByTokenHash(hash(rawToken))
                .filter(candidate -> candidate.isUsable(clock.instant()))
                .orElseThrow(() -> new MembershipException("Invitation is invalid or expired"));

        User user = userRepository.findByEmailIgnoreCase(invitation.getEmail()).orElseGet(() -> {
            if (request.password() == null || request.password().isBlank()) {
                throw new MembershipException("A password is required for a new account");
            }
            return userRepository.save(new User(UUID.randomUUID(), invitation.getEmail(),
                    passwordEncoder.encode(request.password()), request.firstName(), request.lastName()));
        });
        membershipRepository.findByUserIdAndCabinetId(user.getId(), invitation.getCabinetId())
                .ifPresent(existing -> {
                    throw new MembershipException("Membership already exists");
                });
        Instant now = clock.instant();
        if (invitation.getRole() == MembershipRole.KINESITHERAPEUTE) {
            quotaService.ensurePractitionerAvailable(invitation.getCabinetId());
        }
        membershipRepository.save(new Membership(UUID.randomUUID(), user.getId(), invitation.getCabinetId(),
                invitation.getRole(), MembershipStatus.ACTIVE, now));
        invitation.accept(now);
        invitationRepository.save(invitation);
    }

    @Transactional
    public void deactivate(UUID actorId, UUID cabinetId, UUID membershipId) {
        requireAdmin(actorId, cabinetId);
        Membership target = membershipRepository.findById(membershipId)
                .filter(membership -> membership.getCabinetId().equals(cabinetId))
                .orElseThrow(() -> new MembershipException("Membership not found"));
        if (target.getRole() == MembershipRole.ADMIN && target.isActive()
                && membershipRepository.countByCabinetIdAndRoleAndStatus(cabinetId, MembershipRole.ADMIN,
                MembershipStatus.ACTIVE) <= 1) {
            throw new MembershipException("The last cabinet administrator cannot be deactivated");
        }
        target.deactivate();
        membershipRepository.save(target);
    }

    private Membership requireAdmin(UUID actorId, UUID cabinetId) {
        return membershipRepository.findByUserIdAndCabinetId(actorId, cabinetId)
                .filter(Membership::isActive)
                .filter(membership -> membership.getRole() == MembershipRole.ADMIN)
                .orElseThrow(() -> new AuthenticationException());
    }

    private MembershipResponse toResponse(Membership membership) {
        User user = userRepository.findById(membership.getUserId())
                .orElseThrow(() -> new MembershipException("Membership user not found"));
        return new MembershipResponse(membership.getId(), membership.getUserId(), user.getEmail(),
                user.getFirstName(), user.getLastName(), membership.getRole(), membership.getStatus());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash invitation", exception);
        }
    }
}