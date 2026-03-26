package com.budget.repo;

import com.budget.data.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Find all alerts for a user
    List<Alert> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Find unread alerts for a user
    List<Alert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // Find alerts by user and month
    List<Alert> findByUserIdAndMonthYearOrderByCreatedAtDesc(Long userId, String monthYear);

    // Find alerts by user, category and month
    Optional<Alert> findByUserIdAndCategoryAndMonthYearAndAlertType(
            Long userId, 
            String category, 
            String monthYear, 
            Alert.AlertType alertType
    );

    // Check if alert already exists for category in a month
    boolean existsByUserIdAndCategoryAndMonthYearAndAlertType(
            Long userId, 
            String category, 
            String monthYear, 
            Alert.AlertType alertType
    );

    // Count unread alerts
    Long countByUserIdAndIsReadFalse(Long userId);

    // Count critical alerts
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.userId = :userId AND a.isRead = false " +
           "AND a.severity IN ('CRITICAL', 'DANGER')")
    Long countCriticalAlerts(@Param("userId") Long userId);

    // Delete old alerts (for cleanup)
    void deleteByUserIdAndCreatedAtBefore(Long userId, java.time.LocalDateTime date);

    // Find alerts by severity
    List<Alert> findByUserIdAndSeverityOrderByCreatedAtDesc(Long userId, Alert.AlertSeverity severity);

    // Get latest alert for a category
    Optional<Alert> findFirstByUserIdAndCategoryAndMonthYearOrderByCreatedAtDesc(
            Long userId, 
            String category, 
            String monthYear
    );
}
