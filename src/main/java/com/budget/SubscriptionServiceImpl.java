package com.budget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation for Subscription management
 */
@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionServiceImpl.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Override
    public SubscriptionDTO createSubscription(Long userId, SubscriptionDTO dto) {
        logger.info("Creating subscription for user: {} - Service: {}", userId, dto.getServiceName());

        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setServiceName(dto.getServiceName());
        subscription.setDescription(dto.getDescription());
        subscription.setAmount(dto.getAmount());
        subscription.setBillingCycle(dto.getBillingCycle());
        subscription.setCategory(dto.getCategory());
        subscription.setStartDate(dto.getStartDate());
        subscription.setAutoRenewal(dto.getAutoRenewal() != null ? dto.getAutoRenewal() : true);
        subscription.setPaymentMethod(dto.getPaymentMethod());
        subscription.setReminderDaysBefore(dto.getReminderDaysBefore() != null ? dto.getReminderDaysBefore() : 3);
        subscription.setLastUsedDate(dto.getLastUsedDate());
        subscription.setNotes(dto.getNotes());
        subscription.setWebsiteUrl(dto.getWebsiteUrl());
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription saved = subscriptionRepository.save(subscription);
        logger.info("Subscription created successfully with ID: {}", saved.getId());

        return new SubscriptionDTO(saved);
    }

    @Override
    public SubscriptionDTO updateSubscription(Long userId, Long subscriptionId, SubscriptionDTO dto) {
        logger.info("Updating subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);

        subscription.setServiceName(dto.getServiceName());
        subscription.setDescription(dto.getDescription());
        subscription.setAmount(dto.getAmount());
        subscription.setBillingCycle(dto.getBillingCycle());
        subscription.setCategory(dto.getCategory());
        subscription.setStartDate(dto.getStartDate());
        subscription.setAutoRenewal(dto.getAutoRenewal());
        subscription.setPaymentMethod(dto.getPaymentMethod());
        subscription.setReminderDaysBefore(dto.getReminderDaysBefore());
        subscription.setLastUsedDate(dto.getLastUsedDate());
        subscription.setNotes(dto.getNotes());
        subscription.setWebsiteUrl(dto.getWebsiteUrl());

        // Recalculate next renewal date if billing cycle or start date changed
        subscription.calculateNextRenewalDate();

        Subscription updated = subscriptionRepository.save(subscription);
        logger.info("Subscription updated successfully: {}", subscriptionId);

        return new SubscriptionDTO(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionDTO getSubscriptionById(Long userId, Long subscriptionId) {
        logger.debug("Fetching subscription ID: {} for user: {}", subscriptionId, userId);
        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        return new SubscriptionDTO(subscription);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO> getAllSubscriptions(Long userId, Pageable pageable) {
        logger.debug("Fetching all subscriptions for user: {}", userId);
        Page<Subscription> subscriptions = subscriptionRepository.findByUserId(userId, pageable);
        return subscriptions.map(SubscriptionDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO> getSubscriptionsByStatus(Long userId, SubscriptionStatus status, Pageable pageable) {
        logger.debug("Fetching subscriptions for user: {} with status: {}", userId, status);
        Page<Subscription> subscriptions = subscriptionRepository.findByUserIdAndStatus(userId, status, pageable);
        return subscriptions.map(SubscriptionDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SubscriptionDTO> getSubscriptionsByCategory(Long userId, SubscriptionCategory category, Pageable pageable) {
        logger.debug("Fetching subscriptions for user: {} with category: {}", userId, category);
        Page<Subscription> subscriptions = subscriptionRepository.findByUserIdAndCategory(userId, category, pageable);
        return subscriptions.map(SubscriptionDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getActiveSubscriptions(Long userId) {
        logger.debug("Fetching active subscriptions for user: {}", userId);
        List<Subscription> subscriptions = subscriptionRepository.findActiveSubscriptionsByUserId(userId);
        return subscriptions.stream()
                .map(SubscriptionDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getSubscriptionsExpiringSoon(Long userId, int days) {
        logger.debug("Fetching subscriptions expiring within {} days for user: {}", days, userId);
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(days);

        List<Subscription> subscriptions = subscriptionRepository.findSubscriptionsExpiringSoon(userId, startDate, endDate);
        return subscriptions.stream()
                .map(SubscriptionDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> getUnusedSubscriptions(Long userId) {
        logger.debug("Fetching unused subscriptions for user: {}", userId);
        LocalDate thresholdDate = LocalDate.now().minusDays(30);

        List<Subscription> subscriptions = subscriptionRepository.findUnusedSubscriptions(userId, thresholdDate);
        return subscriptions.stream()
                .map(SubscriptionDTO::new)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelSubscription(Long userId, Long subscriptionId) {
        logger.info("Cancelling subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscription.cancel();
        subscriptionRepository.save(subscription);

        logger.info("Subscription cancelled successfully: {}", subscriptionId);
    }

    @Override
    public void pauseSubscription(Long userId, Long subscriptionId) {
        logger.info("Pausing subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscription.pause();
        subscriptionRepository.save(subscription);

        logger.info("Subscription paused successfully: {}", subscriptionId);
    }

    @Override
    public void resumeSubscription(Long userId, Long subscriptionId) {
        logger.info("Resuming subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscription.resume();
        subscriptionRepository.save(subscription);

        logger.info("Subscription resumed successfully: {}", subscriptionId);
    }

    @Override
    public void deleteSubscription(Long userId, Long subscriptionId) {
        logger.info("Deleting subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscriptionRepository.delete(subscription);

        logger.info("Subscription deleted successfully: {}", subscriptionId);
    }

    @Override
    public void updateLastUsedDate(Long userId, Long subscriptionId, LocalDate lastUsedDate) {
        logger.info("Updating last used date for subscription ID: {} to: {}", subscriptionId, lastUsedDate);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscription.setLastUsedDate(lastUsedDate);
        subscriptionRepository.save(subscription);

        logger.info("Last used date updated successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionAnalyticsDTO getSubscriptionAnalytics(Long userId) {
        logger.info("Generating subscription analytics for user: {}", userId);

        List<Subscription> allSubscriptions = subscriptionRepository.findByUserId(userId, Pageable.unpaged()).getContent();
        List<Subscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsByUserId(userId);

        SubscriptionAnalyticsDTO analytics = new SubscriptionAnalyticsDTO();

        // Basic counts
        analytics.setTotalSubscriptions(allSubscriptions.size());
        analytics.setActiveSubscriptions((int) activeSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .count());
        analytics.setCancelledSubscriptions((int) allSubscriptions.stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.CANCELLED)
                .count());

        // Unused subscriptions
        List<Subscription> unusedList = activeSubscriptions.stream()
                .filter(Subscription::isUnused)
                .collect(Collectors.toList());
        analytics.setUnusedSubscriptions(unusedList.size());
        analytics.setUnusedSubscriptionsList(unusedList.stream()
                .map(SubscriptionDTO::new)
                .collect(Collectors.toList()));

        // Calculate costs
        BigDecimal totalMonthlyCost = BigDecimal.ZERO;
        BigDecimal totalAnnualCost = BigDecimal.ZERO;
        BigDecimal potentialSavings = BigDecimal.ZERO;

        for (Subscription sub : activeSubscriptions) {
            BigDecimal monthlyCost = calculateMonthlyCost(sub);
            totalMonthlyCost = totalMonthlyCost.add(monthlyCost);
            totalAnnualCost = totalAnnualCost.add(sub.calculateAnnualCost());

            if (sub.isUnused()) {
                potentialSavings = potentialSavings.add(monthlyCost);
            }
        }

        analytics.setTotalMonthlyCost(totalMonthlyCost.setScale(2, RoundingMode.HALF_UP));
        analytics.setTotalAnnualCost(totalAnnualCost.setScale(2, RoundingMode.HALF_UP));
        analytics.setPotentialSavings(potentialSavings.setScale(2, RoundingMode.HALF_UP));

        // Spending by category
        Map<SubscriptionCategory, SubscriptionAnalyticsDTO.CategorySpending> categorySpending = new HashMap<>();
        for (Subscription sub : activeSubscriptions) {
            SubscriptionCategory category = sub.getCategory();
            SubscriptionAnalyticsDTO.CategorySpending spending = categorySpending.getOrDefault(category, 
                new SubscriptionAnalyticsDTO.CategorySpending(category, 0, BigDecimal.ZERO, BigDecimal.ZERO));

            spending.setCount(spending.getCount() + 1);
            spending.setMonthlySpending(spending.getMonthlySpending().add(calculateMonthlyCost(sub)));
            spending.setAnnualSpending(spending.getAnnualSpending().add(sub.calculateAnnualCost()));

            categorySpending.put(category, spending);
        }

        // Calculate percentages
        for (SubscriptionAnalyticsDTO.CategorySpending spending : categorySpending.values()) {
            if (totalMonthlyCost.compareTo(BigDecimal.ZERO) > 0) {
                double percentage = spending.getMonthlySpending()
                        .divide(totalMonthlyCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
                spending.setPercentageOfTotal(percentage);
            }
        }

        analytics.setSpendingByCategory(categorySpending);

        // Find top category
        if (!categorySpending.isEmpty()) {
            SubscriptionCategory topCategory = categorySpending.entrySet().stream()
                    .max(Comparator.comparing(e -> e.getValue().getMonthlySpending()))
                    .map(Map.Entry::getKey)
                    .orElse(null);
            analytics.setTopCategory(topCategory != null ? topCategory.getDisplayName() : null);
        }

        // Upcoming renewals (next 30 days)
        List<SubscriptionDTO> upcomingRenewals = getSubscriptionsExpiringSoon(userId, 30);
        analytics.setUpcomingRenewals(upcomingRenewals);

        // Subscriptions by billing cycle
        Map<BillingCycle, Integer> billingCycleMap = activeSubscriptions.stream()
                .collect(Collectors.groupingBy(
                        Subscription::getBillingCycle,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
        analytics.setSubscriptionsByBillingCycle(billingCycleMap);

        // Most expensive subscription
        Optional<Subscription> mostExpensive = activeSubscriptions.stream()
                .max(Comparator.comparing(Subscription::calculateAnnualCost));
        mostExpensive.ifPresent(sub -> analytics.setMostExpensiveSubscription(new SubscriptionDTO(sub)));

        logger.info("Analytics generated successfully for user: {}", userId);
        return analytics;
    }

    @Override
    public void renewSubscription(Long userId, Long subscriptionId) {
        logger.info("Renewing subscription ID: {} for user: {}", subscriptionId, userId);

        Subscription subscription = getSubscriptionEntity(userId, subscriptionId);
        subscription.calculateNextRenewalDate();
        subscriptionRepository.save(subscription);

        logger.info("Subscription renewed successfully: {}", subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionDTO> searchByServiceName(Long userId, String serviceName) {
        logger.debug("Searching subscriptions for user: {} with service name: {}", userId, serviceName);
        List<Subscription> subscriptions = subscriptionRepository
                .findByUserIdAndServiceNameContainingIgnoreCase(userId, serviceName);
        return subscriptions.stream()
                .map(SubscriptionDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to get subscription entity and validate user access
     */
    private Subscription getSubscriptionEntity(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        if (!subscription.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized access to subscription: " + subscriptionId);
        }

        return subscription;
    }

    /**
     * Calculate monthly cost from any billing cycle
     */
    private BigDecimal calculateMonthlyCost(Subscription subscription) {
        if (subscription.getAmount() == null) {
            return BigDecimal.ZERO;
        }

        return switch (subscription.getBillingCycle()) {
            case WEEKLY -> subscription.getAmount().multiply(BigDecimal.valueOf(52)).divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case MONTHLY -> subscription.getAmount();
            case QUARTERLY -> subscription.getAmount().divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case HALF_YEARLY -> subscription.getAmount().divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            case YEARLY -> subscription.getAmount().divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
        };
    }
}
