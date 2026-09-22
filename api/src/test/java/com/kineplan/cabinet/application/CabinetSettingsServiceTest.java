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
import com.kineplan.cabinet.api.CabinetSettingsRequest;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetSettings;
import com.kineplan.cabinet.domain.CabinetSettingsRepository;
import com.kineplan.cabinet.domain.CabinetStatus;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CabinetSettingsServiceTest {
    @Mock CabinetRepository cabinetRepository;
    @Mock CabinetSettingsRepository settingsRepository;
    @Mock MembershipRepository membershipRepository;

    private CabinetSettingsService service;
    private UUID userId;
    private UUID cabinetId;

    @BeforeEach
    void setUp() {
        service = new CabinetSettingsService(cabinetRepository, settingsRepository, membershipRepository);
        userId = UUID.randomUUID();
        cabinetId = UUID.randomUUID();
    }

    @Test
    void adminCanUpdateSettings() {
        when(cabinetRepository.findById(cabinetId)).thenReturn(Optional.of(new Cabinet(cabinetId, "Cabinet", null,
                null, "Europe/Paris", CabinetStatus.ACTIF, "ESSENTIAL", Instant.now())));
        when(membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)).thenReturn(Optional.of(
                new Membership(UUID.randomUUID(), userId, cabinetId, MembershipRole.ADMIN,
                        MembershipStatus.ACTIVE, Instant.now())));
        when(settingsRepository.findByCabinetId(cabinetId)).thenReturn(Optional.empty());
        when(settingsRepository.save(any(CabinetSettings.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.update(userId, cabinetId,
                new CabinetSettingsRequest(720, 45, Map.of("smsReminder", "Votre rendez-vous est demain.")));

        assertThat(response.reminderDelayMinutes()).isEqualTo(720);
        assertThat(response.defaultSlotDurationMinutes()).isEqualTo(45);
        assertThat(response.notificationTexts()).containsEntry("smsReminder", "Votre rendez-vous est demain.");
    }

    @Test
    void secretaryCannotUpdateSettings() {
        when(membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)).thenReturn(Optional.of(
                new Membership(UUID.randomUUID(), userId, cabinetId, MembershipRole.SECRETAIRE,
                        MembershipStatus.ACTIVE, Instant.now())));

        assertThatThrownBy(() -> service.update(userId, cabinetId,
                new CabinetSettingsRequest(720, 45, Map.of())))
                .isInstanceOf(CabinetAccessDeniedException.class);
        verify(settingsRepository, never()).save(any());
    }
}
