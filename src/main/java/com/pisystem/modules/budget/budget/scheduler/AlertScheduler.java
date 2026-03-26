package com.budget.scheduler;

import com.budget.service.AlertService;
import com.admin.service.JobStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertService alertService;
    private final JobStatusService jobStatusService;

    /**
     * Check budgets and generate alerts daily at 9:00 PM
     * Runs every day to catch overspending as it happens
     */
    @Scheduled(cron = "0 0 21 * * *") // 9:00 PM daily
    public void checkBudgetsAndGenerateAlerts() {
        if (!jobStatusService.isJobEnabled("BUDGET_ALERTS")) {
            log.info("Skipping BUDGET_ALERTS job as it is currently DISABLED.");
            return;
        }

        log.info("Starting scheduled budget check for alert generation...");
        jobStatusService.updateLastRun("BUDGET_ALERTS");

        String currentMonth = YearMonth.now().toString();

        try {
            // Note: In a production environment, you would iterate through all active users
            // For now, this is a framework - actual user iteration would be implemented
            // based on requirements
            log.info("Scheduled alert check completed for month: {}", currentMonth);

            // TODO: Implement user iteration logic when user management system is ready
            // Example:
            // List<User> activeUsers = userService.getAllActiveUsers();
            // for (User user : activeUsers) {
            // alertService.checkBudgetsAndGenerateAlerts(user.getId(), currentMonth);
            // }

        } catch (Exception e) {
            log.error("Error during scheduled alert generation: {}", e.getMessage(), e);
        }
    }

    /**
     * Alternative: Check budgets on expense creation (event-driven approach)
     * This would be triggered by an event listener instead of a scheduler
     */
    public void checkBudgetOnExpense(Long userId, String monthYear) {
        log.info("Checking budget for user {} after expense in month {}", userId, monthYear);

        try {
            alertService.checkBudgetsAndGenerateAlerts(userId, monthYear);
        } catch (Exception e) {
            log.error("Error checking budget for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
