package com.kineplan.caretype.api;

import java.util.UUID;

public record CareTypeResponse(UUID id, String name, int defaultDurationMinutes, String displayColor) {
}