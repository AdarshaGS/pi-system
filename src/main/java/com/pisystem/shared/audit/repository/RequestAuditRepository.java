package com.pisystem.shared.audit.repository;

import com.pisystem.shared.audit.entity.RequestAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestAuditRepository extends JpaRepository<RequestAudit, Long> {
}
