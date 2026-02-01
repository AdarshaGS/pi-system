package com.budget;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled job for subscription renewal reminders
 * Runs daily at 8:00 AM to check for upcoming renewals and unused subscriptions
 */
@Component
public class SubscriptionReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionReminderScheduler.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    /**
     * Check for subscriptions requiring renewal reminders
     * Runs daily at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * ?") // Every day at 8:00 AM
    public void sendRenewalReminders() {
        logger.info("Starting subscription renewal reminder job...");

        try {
            // Get all active subscriptions that need reminders
            List<Subscription> allActiveSubscriptions = subscriptionRepository.findByNextRenewalDate(LocalDate.now());

            int remindersSent = 0;

            for (Subscription subscription : allActiveSubscriptions) {
                if (subscription.shouldSendRenewalReminder()) {
                    sendRenewalReminder(subscription);
                    remindersSent++;
                }
            }

            // Also check for subscriptions expiring in the next 3 days
            LocalDate threeDaysFromNow = LocalDate.now().plusDays(3);
            List<Subscription> upcomingRenewals = subscriptionRepository.findSubscriptionsExpiringSoon(
                    null, LocalDate.now(), threeDaysFromNow);

            for (Subscription subscription : upcomingRenewals) {
                if (subscription.shouldSendRenewalReminder()) {
                    sendRenewalReminder(subscription);
                    remindersSent++;
                }
            }

            logger.info("Subscription renewal reminder job completed. Reminders sent: {}", remindersSent);

        } catch (Exception e) {
            logger.error("Error in subscription renewal reminder job", e);
        }
    }

    /**
     * Check for unused subscriptions and send alerts
     * Runs weekly on Monday at 9:00 AM
     */
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9:00 AM
    public void checkUnusedSubscriptions() {
        logger.info("Starting unused subscriptions check job...");

        try {
            LocalDate thresholdDate = LocalDate.now().minusDays(30);

            // This would need to be run per user or in batches
            // For now, we'll log the count
            // In production, you'd iterate through users and send notifications

            List<Subscription> allSubscriptions = subscriptionRepository.findAll();
            long unusedCount = allSubscriptions.stream()
                    .filter(Subscription::isUnused)
                    .count();

            logger.info("Found {} potentially unused subscriptions", unusedCount);

            // TODO: Send email/notification to users about unused subscriptions
            // This would require user email service integration

        } catch (Exception e) {
            logger.error("Error in unused subscriptions check job", e);
        }
    }

    /**
     * Mark expired subscriptions
     * Runs daily at 1:00 AM to check for subscriptions that have expired
     */
    @Scheduled(cron = "0 0 1 * * ?") // Every day at 1:00 AM
    public void markExpiredSubscriptions() {
        logger.info("Starting expired subscriptions check job...");

        try {
            List<Subscription> allSubscriptions = subscriptionRepository.findAll();
            int expiredCount = 0;

            for (Subscription subscription : allSubscriptions) {
                // Mark as expired if:
                // 1. Status is ACTIVE
                // 2. Next renewal date has passed
                // 3. Auto-renewal is false
                if (subscription.getStatus() == SubscriptionStatus.ACTIVE &&
                        subscription.getNextRenewalDate() != null &&
                        subscription.getNextRenewalDate().isBefore(LocalDate.now()) &&
                        !subscription.getAutoRenewal()) {

                    subscription.expire();
                    subscriptionRepository.save(subscription);
                    expiredCount++;

                    logger.debug("Marked subscription as expired: ID={}, Service={}",
                            subscription.getId(), subscription.getServiceName());
                }

                // Auto-renew if enabled
                if (subscription.getStatus() == SubscriptionStatus.ACTIVE &&
                        subscription.getNextRenewalDate() != null &&
                        subscription.getNextRenewalDate().isBefore(LocalDate.now()) &&
                        subscription.getAutoRenewal()) {

                    subscription.calculateNextRenewalDate();
                    subscriptionRepository.save(subscription);

                    logger.debug("Auto-renewed subscription: ID={}, Service={}, Next Renewal={}",
                            subscription.getId(), subscription.getServiceName(), subscription.getNextRenewalDate());
                }
            }

            logger.info("Expired subscriptions check completed. Marked {} subscriptions as expired", expiredCount);

        } catch (Exception e) {
            logger.error("Error in expired subscriptions check job", e);
        }
    }

    /**
     * Send renewal reminder (stub for now)
     * In production, this would integrate with email/notification service
     */
    private void sendRenewalReminder(Subscription subscription) {
        // TODO: Integrate with email service or notification service
        logger.info("Renewal reminder for subscription: ID={}, Service={}, User={}, Renewal Date={}, Amount={}",
                subscription.getId(),
                subscription.getServiceName(),
                subscription.getUserId(),
                subscription.getNextRenewalDate(),
                subscription.getAmount());

        // Example of what the reminder message would look like:
        String message = String.format(
                "Reminder: Your %s subscription (₹%.2f) will renew on %s",
                subscription.getServiceName(),
                subscription.getAmount(),
                subscription.getNextRenewalDate()
        );

        logger.debug("Reminder message: {}", message);

        // TODO: Send email/push notification with this message
        // emailService.sendEmail(user.getEmail(), "Subscription Renewal Reminder", message);
        // notificationService.sendPushNotification(user.getId(), message);
    }
}
