package com.kineplan.audit.application;

import com.kineplan.audit.api.AuditLogResponse;
import com.kineplan.audit.domain.AuditLog;
import com.kineplan.audit.domain.AuditLogRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private final AuditLogRepository repository;
    public AuditService(AuditLogRepository repository) { this.repository = repository; }

    @Transactional
    public void record(UUID cabinetId, UUID userId, UUID membershipId, String action, String resource, UUID resourceId) {
        repository.save(new AuditLog(cabinetId, userId, membershipId, action, resource, resourceId));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> list(UUID cabinetId, Pageable pageable) {
        return repository.findByCabinetIdOrderByOccurredAtDesc(cabinetId, pageable).map(this::response);
    }

    private AuditLogResponse response(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUserId(), log.getAction(), log.getResource(),
                log.getResourceId(), log.getOccurredAt());
    }
}