package com.admin.service;

import com.admin.data.ScheduledJob;
import com.admin.repo.ScheduledJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Low-level service for checking and updating job status in the database.
 * Separated from JobManagementService to avoid circular dependencies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobStatusService {

    private final ScheduledJobRepository jobRepository;

    /**
     * Check if a job is enabled in the database
     */
    public boolean isJobEnabled(String jobName) {
        return jobRepository.findByJobName(jobName)
                .map(ScheduledJob::isEnabled)
                .orElse(false);
    }

    /**
     * Update the last run time of a job
     */
    @Transactional
    public void updateLastRun(String jobName) {
        jobRepository.findByJobName(jobName).ifPresent(job -> {
            job.setLastRunAt(LocalDateTime.now());
            jobRepository.save(job);
        });
    }

    /**
     * Toggle the enabled state of a job
     */
    @Transactional
    public void toggleJob(String jobName, boolean enabled) {
        jobRepository.findByJobName(jobName).ifPresent(job -> {
            job.setEnabled(enabled);
            jobRepository.save(job);
            log.info("Job {} has been {}", jobName, enabled ? "ENABLED" : "DISABLED");
        });
    }
}
