package com.kineplan.cabinet.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CabinetSettingsRequest(
        @Min(0) @Max(10080) int reminderDelayMinutes,
        @Min(5) @Max(240) int defaultSlotDurationMinutes,
        @NotNull Map<String, String> notificationTexts) {
}
