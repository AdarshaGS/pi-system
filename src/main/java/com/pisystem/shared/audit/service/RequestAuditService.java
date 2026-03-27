package com.pisystem.shared.audit.service;

import com.pisystem.shared.audit.entity.RequestAudit;
import com.pisystem.shared.audit.repository.RequestAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestAuditService {

    private final RequestAuditRepository requestAuditRepository;

    @Transactional
    public void logRequest(RequestAudit auditLog) {
        try {
            requestAuditRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save request audit log", e);
        }
    }
}
