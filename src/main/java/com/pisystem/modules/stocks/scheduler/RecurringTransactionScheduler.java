package com.investments.stocks.scheduler;

import com.admin.service.JobStatusService;
import com.investments.stocks.service.InvestmentRecurringTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {

    private final InvestmentRecurringTransactionService recurringTransactionService;
    private final JobStatusService jobStatusService;

    public RecurringTransactionScheduler(InvestmentRecurringTransactionService recurringTransactionService,
            JobStatusService jobStatusService) {
        this.recurringTransactionService = recurringTransactionService;
        this.jobStatusService = jobStatusService;
    }

    // Run every day at 1 AM
    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {
        if (!jobStatusService.isJobEnabled("INVESTMENT_RECURRING_TRANSACTIONS")) {
            System.out.println("Skipping INVESTMENT_RECURRING_TRANSACTIONS job as it is currently DISABLED.");
            return;
        }

        System.out.println("Starting recurring transaction processing...");
        jobStatusService.updateLastRun("INVESTMENT_RECURRING_TRANSACTIONS");
        try {
            recurringTransactionService.processDueTransactions();
            System.out.println("Recurring transaction processing completed.");
        } catch (Exception e) {
            System.err.println("Error processing recurring transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
