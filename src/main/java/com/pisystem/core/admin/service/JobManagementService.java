package com.pisystem.core.admin.service;

import com.pisystem.core.admin.dto.ScheduledJobDTO;
import com.pisystem.core.admin.repo.ScheduledJobRepository;
import com.pisystem.infrastructure.alerts.service.AlertProcessorService;
import com.pisystem.modules.budget.SubscriptionReminderScheduler;
import com.pisystem.modules.budget.service.BudgetRecurringTransactionService;
import com.pisystem.modules.budget.scheduler.AlertScheduler;
import com.pisystem.modules.stocks.scheduler.RecurringTransactionScheduler;
import com.pisystem.modules.lending.scheduler.LendingDueDateScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final ScheduledJobRepository jobRepository;
    private final JobStatusService jobStatusService;

    /**
     * Check if a job is enabled in the database
     */
    public boolean isJobEnabled(String jobName) {
        return jobStatusService.isJobEnabled(jobName);
    }

    /**
     * Update the last run time of a job
     */
    public void updateLastRun(String jobName) {
        jobStatusService.updateLastRun(jobName);
    }

    /**
     * Get list of all scheduled jobs from the database
     */
    public List<ScheduledJobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(job -> ScheduledJobDTO.builder()
                        .jobName(job.getJobName())
                        .description(job.getJobDescription())
                        .schedule(job.getCronExpression())
                        .canRunManually(true)
                        .lastRunTime(job.getLastRunAt() != null ? job.getLastRunAt().toString() : "Never")
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Execute a job manually
     */
    @org.springframework.transaction.annotation.Transactional
    public void executeJob(String jobName) {
        log.info("Manually executing job: {}", jobName);

        updateLastRun(jobName);

        try {
            switch (jobName) {
                case "STOCK_PRICE_ALERTS":
                case "ALERT_PROCESSOR":
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
                case "SUBSCRIPTION_REMINDERS":
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
                case "BUDGET_ALERTS":
                    budgetAlertScheduler.checkBudgetsAndGenerateAlerts();
                    break;
                case "STOCK_RECURRING_TRANSACTIONS":
                case "INVESTMENT_RECURRING_TRANSACTIONS":
                    stockRecurringTransactionScheduler.processRecurringTransactions();
                    break;
                case "PORTFOLIO_REBALANCING_ALERTS":
                    alertProcessorService.processTaxDeadlineAlerts();
                    break;
                case "STOCK_PRICE_UPDATE":
                    // Implementation for price update trigger if needed
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

    @org.springframework.transaction.annotation.Transactional
    public void toggleJob(String jobName, boolean enabled) {
        jobRepository.findByJobName(jobName).ifPresent(job -> {
            job.setEnabled(enabled);
            jobRepository.save(job);
            log.info("Job {} has been {}", jobName, enabled ? "ENABLED" : "DISABLED");
        });
    }
}
