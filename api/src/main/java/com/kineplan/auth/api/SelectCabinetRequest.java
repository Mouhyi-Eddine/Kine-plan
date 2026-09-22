package com.kineplan.auth.api;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SelectCabinetRequest(@NotNull UUID cabinetId) {
}