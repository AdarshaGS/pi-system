package com.budget;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Subscription management
 */
public interface SubscriptionService {

    /**
     * Create a new subscription
     */
    SubscriptionDTO createSubscription(Long userId, SubscriptionDTO dto);

    /**
     * Update an existing subscription
     */
    SubscriptionDTO updateSubscription(Long userId, Long subscriptionId, SubscriptionDTO dto);

    /**
     * Get subscription by ID
     */
    SubscriptionDTO getSubscriptionById(Long userId, Long subscriptionId);

    /**
     * Get all subscriptions for a user with pagination
     */
    Page<SubscriptionDTO> getAllSubscriptions(Long userId, Pageable pageable);

    /**
     * Get subscriptions by status
     */
    Page<SubscriptionDTO> getSubscriptionsByStatus(Long userId, SubscriptionStatus status, Pageable pageable);

    /**
     * Get subscriptions by category
     */
    Page<SubscriptionDTO> getSubscriptionsByCategory(Long userId, SubscriptionCategory category, Pageable pageable);

    /**
     * Get active subscriptions
     */
    List<SubscriptionDTO> getActiveSubscriptions(Long userId);

    /**
     * Get subscriptions expiring soon (within next N days)
     */
    List<SubscriptionDTO> getSubscriptionsExpiringSoon(Long userId, int days);

    /**
     * Get unused subscriptions (not used in last 30 days)
     */
    List<SubscriptionDTO> getUnusedSubscriptions(Long userId);

    /**
     * Cancel a subscription
     */
    void cancelSubscription(Long userId, Long subscriptionId);

    /**
     * Pause a subscription
     */
    void pauseSubscription(Long userId, Long subscriptionId);

    /**
     * Resume a paused subscription
     */
    void resumeSubscription(Long userId, Long subscriptionId);

    /**
     * Delete a subscription
     */
    void deleteSubscription(Long userId, Long subscriptionId);

    /**
     * Update last used date for a subscription
     */
    void updateLastUsedDate(Long userId, Long subscriptionId, LocalDate lastUsedDate);

    /**
     * Get subscription analytics
     */
    SubscriptionAnalyticsDTO getSubscriptionAnalytics(Long userId);

    /**
     * Renew a subscription (update next renewal date)
     */
    void renewSubscription(Long userId, Long subscriptionId);

    /**
     * Search subscriptions by service name
     */
    List<SubscriptionDTO> searchByServiceName(Long userId, String serviceName);
}
