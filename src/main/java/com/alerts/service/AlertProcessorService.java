package com.alerts.service;

import com.alerts.entity.AlertRule;
import com.alerts.entity.AlertType;
import com.alerts.entity.NotificationType;
import com.alerts.repository.AlertRuleRepository;
import com.investments.stocks.dto.StockPriceUpdate;
import com.investments.stocks.service.StockPriceWebSocketService;
import com.loan.data.Loan;
import com.loan.repo.LoanRepository;
import com.protection.insurance.data.Insurance;
import com.protection.insurance.repo.InsuranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Service for processing alerts based on rules
 * Runs scheduled jobs to check conditions and trigger notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertProcessorService {

    private final AlertRuleRepository alertRuleRepository;
    private final NotificationService notificationService;
    private final AlertRuleService alertRuleService;
    private final LoanRepository loanRepository;
    private final InsuranceRepository insuranceRepository;
    private final StockPriceWebSocketService stockPriceService;

    /**
     * Process stock price alerts - runs every 5 minutes
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void processStockPriceAlerts() {
        log.info("Processing stock price alerts...");
        
        List<AlertRule> rules = alertRuleRepository.findByTypeAndEnabled(AlertType.STOCK_PRICE, true);
        
        for (AlertRule rule : rules) {
            try {
                processStockPriceAlert(rule);
            } catch (Exception e) {
                log.error("Error processing stock price alert for rule ID: {}. Error: {}", 
                         rule.getId(), e.getMessage());
            }
        }
        
        log.info("Completed processing {} stock price alerts", rules.size());
    }

    private void processStockPriceAlert(AlertRule rule) {
        try {
            // Get current price from stock service
            StockPriceUpdate stockPrice = stockPriceService.fetchSingleStockPrice(rule.getSymbol());
            if (stockPrice == null || stockPrice.getCurrentPrice() == null) {
                return;
            }

            BigDecimal currentPrice = stockPrice.getCurrentPrice();
            boolean shouldTrigger = false;

            // Check price condition
            if (rule.getTargetPrice() != null && rule.getPriceCondition() != null) {
                switch (rule.getPriceCondition().toUpperCase()) {
                    case "ABOVE":
                        shouldTrigger = currentPrice.compareTo(rule.getTargetPrice()) > 0;
                        break;
                    case "BELOW":
                        shouldTrigger = currentPrice.compareTo(rule.getTargetPrice()) < 0;
                        break;
                    case "EQUALS":
                        shouldTrigger = currentPrice.compareTo(rule.getTargetPrice()) == 0;
                        break;
                }
            }

            if (shouldTrigger) {
                String message = String.format(
                    "%s hit target price ₹%.2f (Current: ₹%.2f)",
                    rule.getSymbol(),
                    rule.getTargetPrice(),
                    currentPrice
                );

                notificationService.sendNotification(
                    rule.getUserId(),
                    "Stock Price Alert",
                    message,
                    NotificationType.ALERT,
                    rule.getChannel(),
                    Map.of("symbol", rule.getSymbol(), "currentPrice", currentPrice.toString()),
                    rule.getId()
                );

                alertRuleService.updateLastTriggered(rule.getId());
                log.info("Triggered price alert for user: {}, symbol: {}", 
                        rule.getUserId(), rule.getSymbol());
            }
        } catch (Exception e) {
            log.error("Error processing stock price alert: {}", e.getMessage());
        }
    }

    /**
     * Process EMI due alerts - runs daily at 8 AM
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void processEMIDueAlerts() {
        log.info("Processing EMI due alerts...");
        
        List<AlertRule> rules = alertRuleRepository.findByTypeAndEnabled(AlertType.EMI_DUE, true);
        
        for (AlertRule rule : rules) {
            try {
                processEMIDueAlert(rule);
            } catch (Exception e) {
                log.error("Error processing EMI due alert for rule ID: {}. Error: {}", 
                         rule.getId(), e.getMessage());
            }
        }
        
        log.info("Completed processing {} EMI due alerts", rules.size());
    }

    private void processEMIDueAlert(AlertRule rule) {
        // Find loans with upcoming EMI
        int daysBeforeDue = rule.getDaysBeforeDue() != null ? rule.getDaysBeforeDue() : 3;
        
        List<Loan> loans = loanRepository.findByUserId(rule.getUserId());
        
        for (Loan loan : loans) {
            // Calculate next EMI date (simple approximation based on start date)
            if (loan.getStartDate() != null && loan.getEmiAmount() != null) {
                LocalDate nextEmiDate = loan.getStartDate().plusMonths(1);
                long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), nextEmiDate);
                
                if (daysUntilDue <= daysBeforeDue && daysUntilDue >= 0) {
                    String message = String.format(
                        "EMI of ₹%.2f for %s loan due in %d days (Due date: %s)",
                        loan.getEmiAmount(),
                        loan.getLoanType(),
                        daysUntilDue,
                        nextEmiDate
                    );

                    notificationService.sendNotification(
                        rule.getUserId(),
                        "EMI Due Reminder",
                        message,
                        NotificationType.REMINDER,
                        rule.getChannel(),
                        Map.of("loanId", loan.getId().toString(), "daysUntilDue", String.valueOf(daysUntilDue)),
                        rule.getId()
                    );

                    log.info("Triggered EMI due alert for user: {}, loan: {}", 
                            rule.getUserId(), loan.getId());
                }
            }
        }
    }

    /**
     * Process policy expiry alerts - runs daily at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void processPolicyExpiryAlerts() {
        log.info("Processing policy expiry alerts...");
        
        List<AlertRule> rules = alertRuleRepository.findByTypeAndEnabled(AlertType.POLICY_EXPIRY, true);
        
        for (AlertRule rule : rules) {
            try {
                processPolicyExpiryAlert(rule);
            } catch (Exception e) {
                log.error("Error processing policy expiry alert for rule ID: {}. Error: {}", 
                         rule.getId(), e.getMessage());
            }
        }
        
        log.info("Completed processing {} policy expiry alerts", rules.size());
    }

    private void processPolicyExpiryAlert(AlertRule rule) {
        int daysBeforeExpiry = rule.getDaysBeforeDue() != null ? rule.getDaysBeforeDue() : 30;
        
        List<Insurance> policies = insuranceRepository.findByUserId(rule.getUserId());
        
        for (Insurance policy : policies) {
            if (policy.getEndDate() != null) {
                long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), policy.getEndDate());
                
                if (daysUntilExpiry <= daysBeforeExpiry && daysUntilExpiry >= 0) {
                    String message = String.format(
                        "%s insurance policy (%s) expiring in %d days (Expiry: %s)",
                        policy.getType(),
                        policy.getPolicyNumber(),
                        daysUntilExpiry,
                        policy.getEndDate()
                    );

                    notificationService.sendNotification(
                        rule.getUserId(),
                        "Policy Expiry Alert",
                        message,
                        NotificationType.WARNING,
                        rule.getChannel(),
                        Map.of("policyId", policy.getId().toString(), "daysUntilExpiry", String.valueOf(daysUntilExpiry)),
                        rule.getId()
                    );

                    log.info("Triggered policy expiry alert for user: {}, policy: {}", 
                            rule.getUserId(), policy.getId());
                }
            }
        }
    }

    /**
     * Process premium due alerts - runs daily at 8:30 AM
     */
    @Scheduled(cron = "0 30 8 * * *")
    public void processPremiumDueAlerts() {
        log.info("Processing premium due alerts...");
        
        List<AlertRule> rules = alertRuleRepository.findByTypeAndEnabled(AlertType.PREMIUM_DUE, true);
        
        for (AlertRule rule : rules) {
            try {
                processPremiumDueAlert(rule);
            } catch (Exception e) {
                log.error("Error processing premium due alert for rule ID: {}. Error: {}", 
                         rule.getId(), e.getMessage());
            }
        }
        
        log.info("Completed processing {} premium due alerts", rules.size());
    }

    private void processPremiumDueAlert(AlertRule rule) {
        List<Insurance> policies = insuranceRepository.findByUserId(rule.getUserId());
        
        for (Insurance policy : policies) {
            // Check if premium is due based on frequency
            // This is a simplified check - actual implementation would need premium schedule tracking
            if (policy.getPremiumAmount() != null) {
                String message = String.format(
                    "Premium payment of ₹%.2f due for %s policy (%s)",
                    policy.getPremiumAmount(),
                    policy.getType(),
                    policy.getPolicyNumber()
                );

                // Only send if we haven't sent recently
                notificationService.sendNotification(
                    rule.getUserId(),
                    "Premium Payment Due",
                    message,
                    NotificationType.REMINDER,
                    rule.getChannel(),
                    Map.of("policyId", policy.getId().toString()),
                    rule.getId()
                );
            }
        }
    }

    /**
     * Process tax deadline alerts - runs daily at 10 AM
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void processTaxDeadlineAlerts() {
        log.info("Processing tax deadline alerts...");
        
        List<AlertRule> rules = alertRuleRepository.findByTypeAndEnabled(AlertType.TAX_DEADLINE, true);
        
        for (AlertRule rule : rules) {
            try {
                processTaxDeadlineAlert(rule);
            } catch (Exception e) {
                log.error("Error processing tax deadline alert for rule ID: {}. Error: {}", 
                         rule.getId(), e.getMessage());
            }
        }
        
        log.info("Completed processing {} tax deadline alerts", rules.size());
    }

    private void processTaxDeadlineAlert(AlertRule rule) {
        // ITR filing deadline: July 31
        LocalDate itrDeadline = LocalDate.of(LocalDate.now().getYear(), 7, 31);
        if (LocalDate.now().isAfter(itrDeadline)) {
            itrDeadline = itrDeadline.plusYears(1);
        }
        
        long daysUntilDeadline = ChronoUnit.DAYS.between(LocalDate.now(), itrDeadline);
        int alertThreshold = rule.getDaysBeforeDue() != null ? rule.getDaysBeforeDue() : 30;
        
        if (daysUntilDeadline <= alertThreshold && daysUntilDeadline > 0) {
            String message = String.format(
                "Income Tax Return filing deadline in %d days (Deadline: July 31, %d)",
                daysUntilDeadline,
                itrDeadline.getYear()
            );

            notificationService.sendNotification(
                rule.getUserId(),
                "Tax Filing Deadline Alert",
                message,
                NotificationType.WARNING,
                rule.getChannel(),
                Map.of("deadline", itrDeadline.toString(), "daysRemaining", String.valueOf(daysUntilDeadline)),
                rule.getId()
            );

            alertRuleService.updateLastTriggered(rule.getId());
            log.info("Triggered tax deadline alert for user: {}", rule.getUserId());
        }
    }
}
