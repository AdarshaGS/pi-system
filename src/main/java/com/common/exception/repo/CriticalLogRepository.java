package com.common.exception.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.common.exception.data.CriticalLog;

@Repository
public interface CriticalLogRepository extends JpaRepository<CriticalLog, Long> {
}
