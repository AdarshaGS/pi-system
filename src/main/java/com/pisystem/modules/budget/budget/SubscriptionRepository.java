package com.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Subscription entity
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * Find all subscriptions for a user
     */
    Page<Subscription> findByUserId(Long userId, Pageable pageable);

    /**
     * Find subscriptions by user and status
     */
    Page<Subscription> findByUserIdAndStatus(Long userId, SubscriptionStatus status, Pageable pageable);

    /**
     * Find subscriptions by user and category
     */
    Page<Subscription> findByUserIdAndCategory(Long userId, SubscriptionCategory category, Pageable pageable);

    /**
     * Find active subscriptions for a user
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' ORDER BY s.nextRenewalDate")
    List<Subscription> findActiveSubscriptionsByUserId(@Param("userId") Long userId);

    /**
     * Find subscriptions expiring soon (within specified days)
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' " +
           "AND s.nextRenewalDate BETWEEN :startDate AND :endDate ORDER BY s.nextRenewalDate")
    List<Subscription> findSubscriptionsExpiringSoon(
        @Param("userId") Long userId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    /**
     * Find unused subscriptions (last used date > 30 days ago or never used)
     */
    @Query("SELECT s FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE' " +
           "AND (s.lastUsedDate IS NULL OR s.lastUsedDate < :thresholdDate)")
    List<Subscription> findUnusedSubscriptions(@Param("userId") Long userId, @Param("thresholdDate") LocalDate thresholdDate);

    /**
     * Count active subscriptions for a user
     */
    long countByUserIdAndStatus(Long userId, SubscriptionStatus status);

    /**
     * Find subscriptions by service name (for duplicate check)
     */
    List<Subscription> findByUserIdAndServiceNameContainingIgnoreCase(Long userId, String serviceName);

    /**
     * Find subscriptions requiring renewal reminder
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.nextRenewalDate <= :reminderDate AND s.autoRenewal = true")
    List<Subscription> findSubscriptionsRequiringReminder(@Param("reminderDate") LocalDate reminderDate);

    /**
     * Find all subscriptions expiring on a specific date
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.nextRenewalDate = :date")
    List<Subscription> findByNextRenewalDate(@Param("date") LocalDate date);
}
