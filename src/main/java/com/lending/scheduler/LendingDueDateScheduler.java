package com.lending.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alerts.entity.AlertChannel;
import com.alerts.entity.NotificationType;
import com.alerts.service.NotificationService;
import com.lending.data.LendingRecord;
import com.lending.data.LendingStatus;
import com.lending.repo.LendingRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LendingDueDateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LendingDueDateScheduler.class);

    private final LendingRepository lendingRepository;
    private final NotificationService notificationService;

    // Run every day at 10:00 AM
    @Scheduled(cron = "0 0 10 * * ?")
    public void checkLendingDueDates() {
        logger.info("Starting Lending Due Date Check Job...");

        LocalDate today = LocalDate.now();

        // 1. Check for overdue lendings (Status != PAID and DueDate < Today)
        List<LendingRecord> overdueRecords = lendingRepository.findByStatusNotAndDueDateBefore(LendingStatus.PAID,
                today);
        processOverdueRecords(overdueRecords);

        // 2. Check for lendings due today (Status != PAID and DueDate == Today)
        List<LendingRecord> dueTodayRecords = lendingRepository.findByStatusNotAndDueDate(LendingStatus.PAID, today);
        processDueTodayRecords(dueTodayRecords);

        logger.info("Completed Lending Due Date Check Job.");
    }

    private void processOverdueRecords(List<LendingRecord> records) {
        if (records.isEmpty()) {
            logger.info("No overdue lending records found.");
            return;
        }

        logger.warn("Found {} overdue lending records:", records.size());
        for (LendingRecord record : records) {
            logger.warn("OVERDUE: Borrower '{}' owes {} (Outstanding: {}). Due Date was: {}",
                    record.getBorrowerName(),
                    record.getAmountLent(),
                    record.getOutstandingAmount(),
                    record.getDueDate());

            // Send notification to the lender
            String title = "Lending Overdue: " + record.getBorrowerName();
            String message = String.format(
                "Your lending to %s is overdue! Amount: ₹%.2f, Outstanding: ₹%.2f. Due date was: %s. " +
                "Please follow up with the borrower.",
                record.getBorrowerName(),
                record.getAmountLent(),
                record.getOutstandingAmount(),
                record.getDueDate()
            );

            try {
                notificationService.sendNotification(
                    record.getUserId(),
                    title,
                    message,
                    NotificationType.LENDING_OVERDUE,
                    AlertChannel.IN_APP
                );
            } catch (Exception e) {
                logger.error("Failed to send overdue notification for lending ID: {}", record.getId(), e);
            }
        }
    }

    private void processDueTodayRecords(List<LendingRecord> records) {
        if (records.isEmpty()) {
            logger.info("No lending records due today.");
            return;
        }

        logger.info("Found {} lending records due today:", records.size());
        for (LendingRecord record : records) {
            logger.info("DUE TODAY: Borrower '{}' owes {} (Outstanding: {}).",
                    record.getBorrowerName(),
                    record.getAmountLent(),
                    record.getOutstandingAmount());

            // Send notification to the lender
            String title = "Lending Due Today: " + record.getBorrowerName();
            String message = String.format(
                "Your lending to %s is due today! Amount: ₹%.2f, Outstanding: ₹%.2f. " +
                "Please remind the borrower about the payment.",
                record.getBorrowerName(),
                record.getAmountLent(),
                record.getOutstandingAmount()
            );

            try {
                notificationService.sendNotification(
                    record.getUserId(),
                    title,
                    message,
                    NotificationType.LENDING_DUE,
                    AlertChannel.IN_APP
                );
            } catch (Exception e) {
                logger.error("Failed to send due today notification for lending ID: {}", record.getId(), e);
            }
        }
    }
}
