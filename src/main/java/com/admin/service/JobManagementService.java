package com.admin.service;

import com.admin.dto.ScheduledJobDTO;
import com.alerts.service.AlertProcessorService;
import com.budget.SubscriptionReminderScheduler;
import com.budget.service.BudgetRecurringTransactionService;
import com.budget.scheduler.AlertScheduler;
import com.investments.stocks.scheduler.RecurringTransactionScheduler;
import com.lending.scheduler.LendingDueDateScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing and executing scheduled jobs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobManagementService {

    private final AlertProcessorService alertProcessorService;
    private final SubscriptionReminderScheduler subscriptionReminderScheduler;
    private final BudgetRecurringTransactionService budgetRecurringTransactionService;
    private final AlertScheduler budgetAlertScheduler;
    private final RecurringTransactionScheduler stockRecurringTransactionScheduler;
    private final LendingDueDateScheduler lendingDueDateScheduler;

    /**
     * Get list of all scheduled jobs
     */
    public List<ScheduledJobDTO> getAllJobs() {
        List<ScheduledJobDTO> jobs = new ArrayList<>();

        // Alert Processing Jobs
        jobs.add(ScheduledJobDTO.builder()
                .jobName("STOCK_PRICE_ALERTS")
                .description("Check stock price alerts and send notifications")
                .schedule("Every 5 minutes")
                .category("Alerts")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("LOAN_PAYMENT_ALERTS")
                .description("Check for upcoming loan payments (7 days before)")
                .schedule("Daily at 8:00 AM")
                .category("Alerts")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("INSURANCE_EXPIRY_ALERTS")
                .description("Check for insurance policies expiring soon")
                .schedule("Daily at 9:00 AM")
                .category("Alerts")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("BUDGET_THRESHOLD_ALERTS")
                .description("Check budget threshold alerts (80% spent)")
                .schedule("Daily at 8:30 AM")
                .category("Alerts")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("LENDING_DUE_DATE_CHECK")
                .description("Check for overdue and due today lending records")
                .schedule("Daily at 10:00 AM")
                .category("Alerts")
                .canRunManually(true)
                .build());

        // Subscription Jobs
        jobs.add(ScheduledJobDTO.builder()
                .jobName("SUBSCRIPTION_RENEWAL_REMINDERS")
                .description("Send renewal reminders for active subscriptions")
                .schedule("Daily at 8:00 AM")
                .category("Subscriptions")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("SUBSCRIPTION_UNUSED_ALERTS")
                .description("Alert for unused subscriptions (weekly)")
                .schedule("Every Monday at 9:00 AM")
                .category("Subscriptions")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("SUBSCRIPTION_AUTO_RENEWAL")
                .description("Process automatic subscription renewals")
                .schedule("Daily at 1:00 AM")
                .category("Subscriptions")
                .canRunManually(true)
                .build());

        // Budget Jobs
        jobs.add(ScheduledJobDTO.builder()
                .jobName("BUDGET_RECURRING_TRANSACTIONS")
                .description("Process recurring budget transactions")
                .schedule("Daily at 1:00 AM")
                .category("Budget")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("BUDGET_OVERSPENDING_ALERTS")
                .description("Check for budget overspending and send alerts")
                .schedule("Daily at 9:00 PM")
                .category("Budget")
                .canRunManually(true)
                .build());

        // Investment Jobs
        jobs.add(ScheduledJobDTO.builder()
                .jobName("STOCK_RECURRING_TRANSACTIONS")
                .description("Process recurring stock investment transactions")
                .schedule("Daily at 1:00 AM")
                .category("Investments")
                .canRunManually(true)
                .build());

        jobs.add(ScheduledJobDTO.builder()
                .jobName("PORTFOLIO_REBALANCING_ALERTS")
                .description("Check portfolio rebalancing needs")
                .schedule("Daily at 10:00 AM")
                .category("Investments")
                .canRunManually(true)
                .build());

        return jobs;
    }

    /**
     * Execute a job manually
     */
    public void executeJob(String jobName) {
        log.info("Manually executing job: {}", jobName);

        try {
            switch (jobName) {
                case "STOCK_PRICE_ALERTS":
                    alertProcessorService.processStockPriceAlerts();
                    break;
                case "LOAN_PAYMENT_ALERTS":
                    alertProcessorService.processEMIDueAlerts();
                    break;
                case "INSURANCE_EXPIRY_ALERTS":
                    alertProcessorService.processPolicyExpiryAlerts();
                    break;
                case "BUDGET_THRESHOLD_ALERTS":
                    alertProcessorService.processPremiumDueAlerts();
                    break;
                case "LENDING_DUE_DATE_CHECK":
                    lendingDueDateScheduler.checkLendingDueDates();
                    break;
                case "SUBSCRIPTION_RENEWAL_REMINDERS":
                    subscriptionReminderScheduler.sendRenewalReminders();
                    break;
                case "SUBSCRIPTION_UNUSED_ALERTS":
                    subscriptionReminderScheduler.checkUnusedSubscriptions();
                    break;
                case "SUBSCRIPTION_AUTO_RENEWAL":
                    subscriptionReminderScheduler.markExpiredSubscriptions();
                    break;
                case "BUDGET_RECURRING_TRANSACTIONS":
                    budgetRecurringTransactionService.generateRecurringTransactions();
                    break;
                case "BUDGET_OVERSPENDING_ALERTS":
                    budgetAlertScheduler.checkBudgetsAndGenerateAlerts();
                    break;
                case "STOCK_RECURRING_TRANSACTIONS":
                    stockRecurringTransactionScheduler.processRecurringTransactions();
                    break;
                case "PORTFOLIO_REBALANCING_ALERTS":
                    alertProcessorService.processTaxDeadlineAlerts();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown job: " + jobName);
            }
            log.info("Successfully executed job: {}", jobName);
        } catch (Exception e) {
            log.error("Error executing job {}: {}", jobName, e.getMessage(), e);
            throw new RuntimeException("Failed to execute job: " + e.getMessage());
        }
    }
}
