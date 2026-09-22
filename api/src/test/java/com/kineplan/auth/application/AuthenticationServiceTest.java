package com.kineplan.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.auth.domain.RefreshTokenRepository;
import com.kineplan.auth.domain.User;
import com.kineplan.auth.domain.UserRepository;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetStatus;
import com.kineplan.shared.infrastructure.security.JwtProperties;
import com.kineplan.shared.infrastructure.security.JwtService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AuthenticationServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final MembershipRepository membershipRepository = mock(MembershipRepository.class);
    private final CabinetRepository cabinetRepository = mock(CabinetRepository.class);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final JwtService jwtService = new JwtService(new JwtProperties(
            "01234567890123456789012345678901", Duration.ofMinutes(10), Duration.ofMinutes(2), Duration.ofDays(30)));
    private final AuthenticationService service = new AuthenticationService(userRepository, membershipRepository,
            cabinetRepository, refreshTokenRepository, new BCryptPasswordEncoder(), jwtService,
            new JwtProperties("01234567890123456789012345678901", Duration.ofMinutes(10),
                    Duration.ofMinutes(2), Duration.ofDays(30)));
    private UUID userId;
    private UUID cabinetId;
    private UUID membershipId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cabinetId = UUID.randomUUID();
        membershipId = UUID.randomUUID();
    }

    @Test
    void loginReturnsOnlyActiveCabinets() {
        User user = new User(userId, "user@example.com", new BCryptPasswordEncoder().encode("secret"), "Ada", "Lovelace");
        Membership membership = new Membership(membershipId, userId, cabinetId, MembershipRole.ADMIN,
                MembershipStatus.ACTIVE, Instant.now());
        Cabinet cabinet = mock(Cabinet.class);
        when(cabinet.getId()).thenReturn(cabinetId);
        when(cabinet.getName()).thenReturn("Cabinet A");
        when(cabinet.isActive()).thenReturn(true);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)).thenReturn(List.of(membership));
        when(cabinetRepository.findById(cabinetId)).thenReturn(Optional.of(cabinet));

        var response = service.login(" user@example.com ", "secret");

        assertThat(response.preAuthToken()).isNotBlank();
        assertThat(response.cabinets()).singleElement().satisfies(access -> {
            assertThat(access.id()).isEqualTo(cabinetId);
            assertThat(access.name()).isEqualTo("Cabinet A");
            assertThat(access.role()).isEqualTo(MembershipRole.ADMIN);
        });
    }

    @Test
    void selectionRejectsInactiveCabinet() {
        Cabinet cabinet = mock(Cabinet.class);
        when(cabinet.isActive()).thenReturn(false);
        when(cabinetRepository.findById(cabinetId)).thenReturn(Optional.of(cabinet));

        assertThatThrownBy(() -> service.selectCabinet(userId, cabinetId))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void loginDoesNotExposeSuspendedCabinet() {
        User user = new User(userId, "user@example.com", new BCryptPasswordEncoder().encode("secret"), "Ada", "Lovelace");
        Membership membership = new Membership(membershipId, userId, cabinetId, MembershipRole.ADMIN,
                MembershipStatus.ACTIVE, Instant.now());
        Cabinet cabinet = mock(Cabinet.class);
        when(cabinet.isActive()).thenReturn(false);
        when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        when(membershipRepository.findByUserIdAndStatus(userId, MembershipStatus.ACTIVE)).thenReturn(List.of(membership));
        when(cabinetRepository.findById(cabinetId)).thenReturn(Optional.of(cabinet));

        var response = service.login("user@example.com", "secret");

        assertThat(response.preAuthToken()).isNotBlank();
        assertThat(response.cabinets()).isEmpty();
    }
}