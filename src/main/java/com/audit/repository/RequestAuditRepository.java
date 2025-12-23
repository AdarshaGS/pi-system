package com.audit.repository;

import com.audit.entity.RequestAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestAuditRepository extends JpaRepository<RequestAudit, Long> {
}
