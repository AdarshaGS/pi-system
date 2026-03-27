package com.pisystem.shared.exception.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.pisystem.shared.exception.data.CriticalLog;
import java.util.List;

@Repository
public interface CriticalLogRepository extends JpaRepository<CriticalLog, Long> {
    
    @Query("SELECT c FROM CriticalLog c ORDER BY c.timestamp DESC")
    List<CriticalLog> findTop10ByOrderByTimestampDesc();
}
