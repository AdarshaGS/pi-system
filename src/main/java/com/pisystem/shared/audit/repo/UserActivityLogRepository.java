package com.pisystem.shared.audit.repo;

import com.pisystem.shared.audit.data.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    List<UserActivityLog> findByUserIdOrderByTimestampDesc(Long userId);

    List<UserActivityLog> findByActionOrderByTimestampDesc(String action);

    List<UserActivityLog> findTop50ByOrderByTimestampDesc();

    List<UserActivityLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    List<UserActivityLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(Long userId, LocalDateTime start,
            LocalDateTime end);
}
