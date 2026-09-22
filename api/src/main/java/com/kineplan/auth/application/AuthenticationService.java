package com.kineplan.auth.application;

import com.kineplan.auth.api.CabinetAccessResponse;
import com.kineplan.auth.api.ChangePasswordRequest;
import com.kineplan.auth.api.LoginResponse;
import com.kineplan.auth.api.TokenResponse;
import com.kineplan.auth.api.UserProfileResponse;
import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.auth.domain.RefreshToken;
import com.kineplan.auth.domain.RefreshTokenRepository;
import com.kineplan.auth.domain.User;
import com.kineplan.auth.domain.UserRepository;
import com.kineplan.auth.domain.UserStatus;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final MembershipRepository membershipRepository;
    private final CabinetRepository cabinetRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.kineplan.shared.infrastructure.security.JwtService jwtService;
    private final com.kineplan.shared.infrastructure.security.JwtProperties jwtProperties;
    private final Clock clock;

    public AuthenticationService(UserRepository userRepository, MembershipRepository membershipRepository,
                                 CabinetRepository cabinetRepository, RefreshTokenRepository refreshTokenRepository,
                                 PasswordEncoder passwordEncoder,
                                 com.kineplan.shared.infrastructure.security.JwtService jwtService,
                                 com.kineplan.shared.infrastructure.security.JwtProperties jwtProperties) {
        this.userRepository = userRepository;
        this.membershipRepository = membershipRepository;
        this.cabinetRepository = cabinetRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.clock = Clock.systemUTC();
    }

    @Transactional
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(password, candidate.getPasswordHash()))
                .orElseThrow(AuthenticationException::new);

        Instant now = clock.instant();
        user.markLogin(now);
        List<CabinetAccessResponse> cabinets = membershipRepository
                .findByUserIdAndStatus(user.getId(), MembershipStatus.ACTIVE)
                .stream()
                .map(this::toCabinetAccess)
            .filter(java.util.Objects::nonNull)
                .toList();
        return new LoginResponse(jwtService.createPreAuthToken(user.getId()), cabinets);
    }

    @Transactional
    public TokenResponse selectCabinet(UUID userId, UUID cabinetId) {
        Membership membership = activeMembership(userId, cabinetId);
        return issueTokens(userId, membership);
    }

    @Transactional
    public TokenResponse refresh(String rawRefreshToken) {
        io.jsonwebtoken.Claims claims;
        try {
            claims = jwtService.parse(rawRefreshToken);
        } catch (RuntimeException exception) {
            throw new AuthenticationException();
        }
        if (!"refresh".equals(claims.get("token_type", String.class))) {
            throw new AuthenticationException();
        }

        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .filter(token -> token.isUsable(clock.instant()))
                .orElseThrow(AuthenticationException::new);
        UUID userId = UUID.fromString(claims.getSubject());
        UUID cabinetId = UUID.fromString(claims.get("cabinet_id", String.class));
        UUID membershipId = UUID.fromString(claims.get("membership_id", String.class));
        if (!stored.getUserId().equals(userId) || !stored.getCabinetId().equals(cabinetId)
                || !stored.getMembershipId().equals(membershipId)) {
            throw new AuthenticationException();
        }

        Membership membership = membershipRepository.findById(membershipId)
                .filter(candidate -> candidate.isActive() && candidate.getUserId().equals(userId)
                        && candidate.getCabinetId().equals(cabinetId))
                .orElseThrow(AuthenticationException::new);
        ensureActiveCabinet(cabinetId);

        IssuedTokens replacement = issueTokensInternal(userId, membership);
        stored.rotate(replacement.id(), clock.instant());
        refreshTokenRepository.save(stored);
        return replacement.response();
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .ifPresent(token -> token.revoke(clock.instant()));
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow(AuthenticationException::new);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException();
        }
        user.changePassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse me(UUID userId, UUID cabinetId, UUID membershipId) {
        User user = userRepository.findById(userId).orElseThrow(AuthenticationException::new);
        Membership membership = membershipRepository.findById(membershipId)
                .filter(candidate -> candidate.isActive() && candidate.getUserId().equals(userId)
                        && candidate.getCabinetId().equals(cabinetId))
                .orElseThrow(AuthenticationException::new);
        Cabinet cabinet = ensureActiveCabinet(cabinetId);
        return new UserProfileResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPhone(), cabinet.getId(), cabinet.getName(), membership.getId(), membership.getRole());
    }

    private TokenResponse issueTokens(UUID userId, Membership membership) {
        return issueTokensInternal(userId, membership).response();
    }

    private IssuedTokens issueTokensInternal(UUID userId, Membership membership) {
        ensureActiveCabinet(membership.getCabinetId());
        String accessToken = jwtService.createAccessToken(userId, membership.getCabinetId(),
                membership.getId(), membership.getRole().name());
        String refreshToken = jwtService.createRefreshToken(userId, membership.getCabinetId(), membership.getId());
        Instant issuedAt = clock.instant();
        UUID refreshTokenId = UUID.randomUUID();
        refreshTokenRepository.save(new RefreshToken(refreshTokenId, hash(refreshToken), userId,
                membership.getCabinetId(), membership.getId(), issuedAt,
                issuedAt.plus(jwtProperties.refreshTokenTtl())));
        return new IssuedTokens(refreshTokenId,
                new TokenResponse(accessToken, refreshToken, jwtProperties.accessTokenTtl().toSeconds()));
    }

    private Membership activeMembership(UUID userId, UUID cabinetId) {
        ensureActiveCabinet(cabinetId);
        return membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)
                .filter(Membership::isActive)
                .orElseThrow(AuthenticationException::new);
    }

    private Cabinet ensureActiveCabinet(UUID cabinetId) {
        return cabinetRepository.findById(cabinetId)
                .filter(Cabinet::isActive)
                .orElseThrow(AuthenticationException::new);
    }

    private CabinetAccessResponse toCabinetAccess(Membership membership) {
        Cabinet cabinet = cabinetRepository.findById(membership.getCabinetId())
                .filter(Cabinet::isActive)
                .orElse(null);
        return cabinet == null ? null : new CabinetAccessResponse(cabinet.getId(), cabinet.getName(), membership.getRole());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash token", exception);
        }
    }

    private record IssuedTokens(UUID id, TokenResponse response) {
    }
}