package com.kineplan.cabinet.api;

import java.util.Map;
import java.util.UUID;

public record CabinetSettingsResponse(
        UUID cabinetId,
        int reminderDelayMinutes,
        int defaultSlotDurationMinutes,
        Map<String, String> notificationTexts) {
}
