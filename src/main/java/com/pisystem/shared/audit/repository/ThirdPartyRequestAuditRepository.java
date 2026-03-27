package com.pisystem.shared.audit.repository;

import com.pisystem.shared.audit.entity.ThirdPartyRequestAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThirdPartyRequestAuditRepository extends JpaRepository<ThirdPartyRequestAudit, Long> {
}
