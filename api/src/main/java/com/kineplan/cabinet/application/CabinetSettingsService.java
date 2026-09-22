package com.kineplan.cabinet.application;

import com.kineplan.auth.domain.Membership;
import com.kineplan.auth.domain.MembershipRepository;
import com.kineplan.auth.domain.MembershipRole;
import com.kineplan.auth.domain.MembershipStatus;
import com.kineplan.cabinet.api.CabinetSettingsRequest;
import com.kineplan.cabinet.api.CabinetSettingsResponse;
import com.kineplan.cabinet.domain.CabinetRepository;
import com.kineplan.cabinet.domain.CabinetSettings;
import com.kineplan.cabinet.domain.CabinetSettingsRepository;
import java.util.HashMap;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CabinetSettingsService {
    private final CabinetRepository cabinetRepository;
    private final CabinetSettingsRepository settingsRepository;
    private final MembershipRepository membershipRepository;

    public CabinetSettingsService(CabinetRepository cabinetRepository, CabinetSettingsRepository settingsRepository,
                                  MembershipRepository membershipRepository) {
        this.cabinetRepository = cabinetRepository;
        this.settingsRepository = settingsRepository;
        this.membershipRepository = membershipRepository;
    }

    @Transactional
    public CabinetSettingsResponse get(UUID userId, UUID cabinetId) {
        requireActiveMembership(userId, cabinetId);
        ensureCabinet(cabinetId);
        return response(settingsRepository.findByCabinetId(cabinetId).orElseGet(() ->
                settingsRepository.save(new CabinetSettings(cabinetId, 1440, 30, new HashMap<>()))));
    }

    @Transactional
    public CabinetSettingsResponse update(UUID userId, UUID cabinetId, CabinetSettingsRequest request) {
        Membership membership = requireActiveMembership(userId, cabinetId);
        if (membership.getRole() != MembershipRole.ADMIN) {
            throw new CabinetAccessDeniedException();
        }
        ensureCabinet(cabinetId);
        CabinetSettings settings = settingsRepository.findByCabinetId(cabinetId)
                .orElseGet(() -> new CabinetSettings(cabinetId, 1440, 30, new HashMap<>()));
        settings.update(request.reminderDelayMinutes(), request.defaultSlotDurationMinutes(), request.notificationTexts());
        return response(settingsRepository.save(settings));
    }

    private Membership requireActiveMembership(UUID userId, UUID cabinetId) {
        return membershipRepository.findByUserIdAndCabinetId(userId, cabinetId)
                .filter(membership -> membership.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(CabinetAccessDeniedException::new);
    }

    private void ensureCabinet(UUID cabinetId) {
        cabinetRepository.findById(cabinetId).orElseThrow(CabinetNotFoundException::new);
    }

    private CabinetSettingsResponse response(CabinetSettings settings) {
        return new CabinetSettingsResponse(settings.getCabinetId(), settings.getReminderDelayMinutes(),
                settings.getDefaultSlotDurationMinutes(), settings.getNotificationTexts());
    }
}
