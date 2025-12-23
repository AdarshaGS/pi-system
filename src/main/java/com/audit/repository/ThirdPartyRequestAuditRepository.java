package com.audit.repository;

import com.audit.entity.ThirdPartyRequestAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdPartyRequestAuditRepository extends JpaRepository<ThirdPartyRequestAudit, Long> {
}
