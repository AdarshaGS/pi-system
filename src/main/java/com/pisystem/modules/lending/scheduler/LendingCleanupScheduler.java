package com.pisystem.modules.lending.scheduler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.pisystem.core.admin.service.JobStatusService;
import com.pisystem.modules.lending.data.LendingRecord;
import com.pisystem.modules.lending.data.LendingStatus;
import com.pisystem.modules.lending.repo.LendingRepository;

import lombok.RequiredArgsConstructor;

/**
 * Scheduled job that purges fully paid lending records across all users.
 *
 * <p>Schedule: runs every Sunday at 2:00 AM (low-traffic window).
 * Controlled via the admin job toggle {@code LENDING_PAID_CLEANUP}.
 *
 * <p>Logic:
 * <ul>
 *   <li>Status == PAID  → delete (cascade removes repayments via orphanRemoval)</li>
 *   <li>Status != PAID  → keep untouched</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LendingCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LendingCleanupScheduler.class);

    private static final String JOB_NAME = "LENDING_PAID_CLEANUP";

    private final LendingRepository lendingRepository;
    private final JobStatusService jobStatusService;

    /**
     * Runs every Sunday at 2:00 AM.
     * Cron: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 2 * * SUN")
    @Transactional
    public void deletePaidLendingRecords() {
        if (!jobStatusService.isJobEnabled(JOB_NAME)) {
            logger.info("Skipping {} job — currently DISABLED.", JOB_NAME);
            return;
        }

        logger.info("Starting {} job...", JOB_NAME);
        jobStatusService.updateLastRun(JOB_NAME);

        List<LendingRecord> paidRecords = lendingRepository.findByStatus(LendingStatus.PAID);

        if (paidRecords.isEmpty()) {
            logger.info("No fully paid lending records found. Nothing to delete.");
            return;
        }

        logger.info("Found {} fully paid lending records to delete.", paidRecords.size());

        for (LendingRecord record : paidRecords) {
            logger.info("Deleting paid record — id={}, borrower='{}', userId={}, amountLent={}, dateLent={}",
                    record.getId(),
                    record.getBorrowerName(),
                    record.getUserId(),
                    record.getAmountLent(),
                    record.getDateLent());
        }

        lendingRepository.deleteAll(paidRecords);

        logger.info("Completed {} job. Deleted {} records.", JOB_NAME, paidRecords.size());
    }
}
