package com.alerts.repository;

import com.alerts.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for UserNotification entity
 */
@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {

    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<UserNotification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    Long countByUserIdAndIsRead(Long userId, Boolean isRead);

    List<UserNotification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
            Long userId, LocalDateTime after);

    void deleteByUserIdAndId(Long userId, Long id);
}
