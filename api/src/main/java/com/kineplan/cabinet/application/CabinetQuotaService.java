package com.kineplan.cabinet.application;

import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.cabinet.api.CabinetQuotasResponse;
import com.kineplan.cabinet.domain.Cabinet;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.infrastructure.SubscriptionPlanProperties;
import com.kineplan.notification.infrastructure.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CabinetQuotaService {
    private final CabinetRepository cabinetRepository;
    private final MembershipRepository membershipRepository;
    private final NotificationDeliveryRepository deliveryRepository;
    private final SubscriptionPlanProperties planProperties;
    private final Clock clock = Clock.systemUTC();

    public CabinetQuotaService(CabinetRepository cabinetRepository, MembershipRepository membershipRepository,
                               NotificationDeliveryRepository deliveryRepository,
                               SubscriptionPlanProperties planProperties) {
        this.cabinetRepository = cabinetRepository;
        this.membershipRepository = membershipRepository;
        this.deliveryRepository = deliveryRepository;
        this.planProperties = planProperties;
    }

    @Transactional(readOnly = true)
    public CabinetQuotasResponse get(UUID userId, UUID cabinetId) {
        membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .filter(membership -> membership.getRole() == MembershipRole.ADMIN)
                .orElseThrow(CabinetAccessDeniedException::new);
        return getForCabinet(cabinetId);
    }

    @Transactional(readOnly = true)
    public CabinetQuotasResponse getForCabinet(UUID cabinetId) {
        Cabinet cabinet = cabinetRepository.findById(cabinetId).orElseThrow(CabinetNotFoundException::new);
        SubscriptionPlanProperties.Quota quota = planProperties.quotaFor(cabinet.getSubscriptionPlan());
        Instant monthStart = clock.instant().atZone(ZoneOffset.UTC).with(TemporalAdjusters.firstDayOfMonth())
                .toLocalDate().atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant nextMonth = monthStart.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
        long activePractitioners = membershipRepository.countByCabinetIdAndRoleAndStatus(
                cabinetId, MembershipRole.KINESITHERAPEUTE, MembershipStatus.ACTIVE);
        long smsSent = deliveryRepository.countByCabinetIdAndChannelAndStatusAndSentAtGreaterThanEqualAndSentAtLessThan(
                cabinetId, "SMS", "SENT", monthStart, nextMonth);
        return new CabinetQuotasResponse(cabinet.getSubscriptionPlan(), activePractitioners,
                quota.getMaxPractitioners(), smsSent, quota.getMaxSmsPerMonth());
    }

    @Transactional(readOnly = true)
    public void ensurePractitionerAvailable(UUID cabinetId) {
        CabinetQuotasResponse quotas = getForCabinet(cabinetId);
        if (quotas.activePractitioners() >= quotas.maxPractitioners()) {
            throw new CabinetException("Practitioner quota exceeded");
        }
    }

    @Transactional(readOnly = true)
    public void ensureSmsAvailable(UUID cabinetId) {
        CabinetQuotasResponse quotas = getForCabinet(cabinetId);
        if (quotas.smsSentThisMonth() >= quotas.maxSmsPerMonth()) {
            throw new CabinetException("SMS quota exceeded");
        }
    }
}
