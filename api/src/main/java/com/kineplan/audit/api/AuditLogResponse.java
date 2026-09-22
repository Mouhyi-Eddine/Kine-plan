package com.kineplan.audit.api;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(UUID id, UUID userId, String action, String resource, UUID resourceId, Instant occurredAt) { }