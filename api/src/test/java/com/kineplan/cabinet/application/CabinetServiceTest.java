package com.kineplan.cabinet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.auth.domain.User;
import com.kineplan.auth.domain.UserRepository;
import com.kineplan.cabinet.api.OnboardingRequest;
import com.kineplan.cabinet.api.OnboardingResponse;
import com.kineplan.cabinet.api.UpdateCabinetRequest;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetStatus;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CabinetServiceTest {
    @Mock CabinetRepository cabinetRepository;
    @Mock MembershipRepository membershipRepository;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    private CabinetService service;

    @BeforeEach
    void setUp() {
        service = new CabinetService(cabinetRepository, membershipRepository, userRepository, passwordEncoder);
    }

    @Test
    void onboardingCreatesActiveCabinetAndFirstAdmin() {
        when(cabinetRepository.save(any(Cabinet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(membershipRepository.save(any(Membership.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("a-strong-password")).thenReturn("hash");

        OnboardingResponse response = service.onboard(new OnboardingRequest(
                "Cabinet Test", "1 rue Test", "12345678901234", "Europe/Paris",
                "admin@example.com", "a-strong-password", "Ada", "Lovelace"));

        assertThat(response.cabinetId()).isNotNull();
        assertThat(response.membershipId()).isNotNull();
        ArgumentCaptor<Cabinet> cabinet = ArgumentCaptor.forClass(Cabinet.class);
        verify(cabinetRepository).save(cabinet.capture());
        assertThat(cabinet.getValue().getStatus()).isEqualTo(CabinetStatus.ACTIF);
        assertThat(cabinet.getValue().getSubscriptionPlan()).isEqualTo("ESSENTIAL");
        ArgumentCaptor<Membership> membership = ArgumentCaptor.forClass(Membership.class);
        verify(membershipRepository).save(membership.capture());
        assertThat(membership.getValue().getRole()).isEqualTo(MembershipRole.ADMIN);
        assertThat(membership.getValue().getStatus()).isEqualTo(MembershipStatus.ACTIVE);
    }

    @Test
    void existingAccountWithWrongPasswordGetsGenericOnboardingFailure() {
        User existing = new User(UUID.randomUUID(), "admin@example.com", "hash", "Existing", "User");
        when(userRepository.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.onboard(new OnboardingRequest(
                "Cabinet Test", null, null, "Europe/Paris", "admin@example.com",
                "wrong-password", "Ada", "Lovelace")))
                .isInstanceOf(CabinetException.class)
                .hasMessage("Unable to complete onboarding");
        verify(cabinetRepository, never()).save(any());
    }

    @Test
    void nonAdminCannotUpdateCabinetProfile() {
        UUID userId = UUID.randomUUID();
        UUID cabinetId = UUID.randomUUID();
        Membership membership = new Membership(UUID.randomUUID(), userId, cabinetId,
                MembershipRole.SECRETAIRE, MembershipStatus.ACTIVE, Instant.now());
        when(membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)).thenReturn(Optional.of(membership));

        assertThatThrownBy(() -> service.update(userId, cabinetId,
                new UpdateCabinetRequest("Nouveau nom", null, null, "Europe/Paris")))
                .isInstanceOf(CabinetAccessDeniedException.class);
        verify(cabinetRepository, never()).save(any());
    }
}
