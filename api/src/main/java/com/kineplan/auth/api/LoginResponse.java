package com.kineplan.auth.api;

import java.util.List;

public record LoginResponse(String preAuthToken, List<CabinetAccessResponse> cabinets) {
}