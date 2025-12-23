package com.audit.service;

import com.audit.entity.ThirdPartyRequestAudit;
import com.audit.repository.ThirdPartyRequestAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThirdPartyAuditService {

    private final ThirdPartyRequestAuditRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOnly(ThirdPartyRequestAudit audit) {
        try {
            repository.save(audit);
        } catch (Exception e) {
            log.error("Failed to save third party audit log: {}", e.getMessage());
        }
    }
}
