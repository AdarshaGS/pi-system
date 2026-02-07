package com.investments.stocks.scheduler;

import com.investments.stocks.service.InvestmentRecurringTransactionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionScheduler {
    
    private final InvestmentRecurringTransactionService recurringTransactionService;
    
    public RecurringTransactionScheduler(InvestmentRecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }
    
    // Run every day at 1 AM
    @Scheduled(cron = "0 0 1 * * *")
    public void processRecurringTransactions() {
        System.out.println("Starting recurring transaction processing...");
        try {
            recurringTransactionService.processDueTransactions();
            System.out.println("Recurring transaction processing completed.");
        } catch (Exception e) {
            System.err.println("Error processing recurring transactions: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
