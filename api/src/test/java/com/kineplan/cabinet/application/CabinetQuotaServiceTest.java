package com.kineplan.cabinet.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetStatus;
import com.kineplan.cabinet.infrastructure.SubscriptionPlanProperties;
import com.kineplan.notification.infrastructure.NotificationDeliveryRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CabinetQuotaServiceTest {
    @Mock CabinetRepository cabinetRepository;
    @Mock MembershipRepository membershipRepository;
    @Mock NotificationDeliveryRepository deliveryRepository;

    private CabinetQuotaService service;
    private UUID userId;
    private UUID cabinetId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cabinetId = UUID.randomUUID();
        SubscriptionPlanProperties properties = new SubscriptionPlanProperties();
        SubscriptionPlanProperties.Quota quota = new SubscriptionPlanProperties.Quota();
        quota.setMaxPractitioners(3);
        quota.setMaxSmsPerMonth(500);
        properties.setPlans(java.util.Map.of("ESSENTIAL", quota));
        service = new CabinetQuotaService(cabinetRepository, membershipRepository, deliveryRepository, properties);
        when(membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)).thenReturn(Optional.of(
                new Membership(UUID.randomUUID(), userId, cabinetId, MembershipRole.ADMIN,
                        MembershipStatus.ACTIVE, Instant.now())));
    }

    @Test
    void returnsConfiguredPlanQuotasAndUsage() {
        when(cabinetRepository.findById(cabinetId)).thenReturn(Optional.of(new Cabinet(cabinetId, "Cabinet", null,
                null, "Europe/Paris", CabinetStatus.ACTIF, "ESSENTIAL", Instant.now())));
        when(membershipRepository.countByCabinetIdAndRoleAndStatus(cabinetId, MembershipRole.KINESITHERAPEUTE,
                MembershipStatus.ACTIVE)).thenReturn(2L);
        when(deliveryRepository.countByCabinetIdAndChannelAndStatusAndSentAtGreaterThanEqualAndSentAtLessThan(
                org.mockito.ArgumentMatchers.eq(cabinetId), org.mockito.ArgumentMatchers.eq("SMS"),
                org.mockito.ArgumentMatchers.eq("SENT"), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any())).thenReturn(12L);
        var response = service.get(userId, cabinetId);

        assertThat(response.subscriptionPlan()).isEqualTo("ESSENTIAL");
        assertThat(response.activePractitioners()).isEqualTo(2);
        assertThat(response.maxPractitioners()).isEqualTo(3);
        assertThat(response.smsSentThisMonth()).isEqualTo(12);
        assertThat(response.maxSmsPerMonth()).isEqualTo(500);
    }

    @Test
    void rejectsQuotaReadForNonAdmin() {
        when(membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)).thenReturn(Optional.of(
                new Membership(UUID.randomUUID(), userId, cabinetId, MembershipRole.SECRETAIRE,
                        MembershipStatus.ACTIVE, Instant.now())));

        assertThatThrownBy(() -> service.get(userId, cabinetId))
                .isInstanceOf(CabinetAccessDeniedException.class);
    }
}
