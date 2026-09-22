package com.kineplan.appointment.api;

import java.util.List;
import java.util.UUID;

public record AppointmentSeriesResponse(UUID id, List<AppointmentResponse> appointments) {
}