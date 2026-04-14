package com.pisystem.infrastructure.migration.service;

import com.pisystem.infrastructure.migration.config.MigrationProperties;
import com.pisystem.infrastructure.migration.domain.MigrationHistory;
import com.pisystem.infrastructure.migration.domain.MigrationStatus;
import com.pisystem.infrastructure.migration.domain.MigrationType;
import com.pisystem.infrastructure.migration.domain.SchemaDrift;
import com.pisystem.infrastructure.migration.repository.MigrationHistoryRepository;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Main migration management service
 * Orchestrates migration execution, validation, and monitoring
 */
@Service
public class MigrationManagerService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationManagerService.class);

    private final Flyway flyway;
    private final MigrationHistoryRepository migrationHistoryRepository;
    private final MigrationValidationService validationService;
    private final SchemaDriftDetectionService driftDetectionService;
    private final BaselineGeneratorService baselineGeneratorService;
    private final MigrationProperties properties;

    @Value("${spring.application.name:unknown}")
    private String applicationName;

    private volatile MigrationStatus lastExecutionStatus = MigrationStatus.PENDING;
    private volatile String lastExecutionMessage;

    @Autowired
    public MigrationManagerService(
            Flyway flyway,
            MigrationHistoryRepository migrationHistoryRepository,
            MigrationValidationService validationService,
            SchemaDriftDetectionService driftDetectionService,
            BaselineGeneratorService baselineGeneratorService,
            MigrationProperties properties) {
        this.flyway = flyway;
        this.migrationHistoryRepository = migrationHistoryRepository;
        this.validationService = validationService;
        this.driftDetectionService = driftDetectionService;
        this.baselineGeneratorService = baselineGeneratorService;
        this.properties = properties;
    }

    /**
     * Initialize migrations on application startup
     */
    @PostConstruct
    public void initializeMigrations() {
        logger.info("=".repeat(80));
        logger.info("MIGRATION SYSTEM INITIALIZATION");
        logger.info("=".repeat(80));

        try {
            // Step 1: Execute migrations FIRST (so migration_history table exists)
            if (properties.isAsyncExecution()) {
                executeMigrationsAsync();
            } else {
                executeMigrations();
            }

            // Step 2: Validate existing migrations (after migrations run)
            if (properties.isValidationEnabled()) {
                List<String> validationErrors = validationService.validateMigrations();

                if (!validationErrors.isEmpty()) {
                    logger.error("Migration validation failed with {} errors:", validationErrors.size());
                    validationErrors.forEach(logger::error);

                    if (properties.isFailOnValidationError()) {
                        logger.warn("Validation errors detected but continuing startup (set fail-on-validation-error: false to suppress this warning)");
                    }
                }
            }

            // Step 3: Detect schema drift
            if (properties.isDriftDetectionEnabled()) {
                Set<String> expectedTables = driftDetectionService.getExpectedTablesFromMigrations();
                SchemaDrift drift = driftDetectionService.detectDrift(expectedTables);

                if (drift.hasDrift()) {
                    logger.warn("Schema drift detected: {}", drift);

                    if (properties.isFailOnDrift()) {
                        throw new IllegalStateException("Schema drift detected - application startup aborted");
                    }
                }
            }

            // Step 4: Generate baseline if enabled
            if (properties.isBaselineEnabled()) {
                generateBaseline();
            }

            logger.info("=".repeat(80));
            logger.info("MIGRATION SYSTEM INITIALIZATION COMPLETE");
            logger.info("=".repeat(80));

        } catch (Exception e) {
            logger.error("Migration initialization failed", e);
            lastExecutionStatus = MigrationStatus.FAILED;
            lastExecutionMessage = e.getMessage();

            if (properties.isFailOnValidationError()) {
                throw new RuntimeException("Migration system initialization failed", e);
            }
        }
    }

    /**
     * Execute migrations synchronously
     */
    public void executeMigrations() {
        logger.info("Executing migrations (synchronous)...");
        long startTime = System.currentTimeMillis();

        try {
            lastExecutionStatus = MigrationStatus.RUNNING;

            // Get info before migration
            MigrationInfo[] pending = flyway.info().pending();
            logger.info("Found {} pending migrations", pending.length);

            // Execute migrations
            int migrationsExecuted = flyway.migrate().migrationsExecuted;

            long executionTime = System.currentTimeMillis() - startTime;

            logger.info("Successfully executed {} migrations in {}ms", migrationsExecuted, executionTime);

            if (executionTime > properties.getLongRunningThresholdSeconds() * 1000) {
                logger.warn("Migration execution took longer than threshold: {}ms", executionTime);
            }

            // Record in history
            recordMigrationExecution(pending, MigrationStatus.SUCCESS, executionTime);

            lastExecutionStatus = MigrationStatus.SUCCESS;
            lastExecutionMessage = "Executed " + migrationsExecuted + " migrations";

        } catch (Exception e) {
            logger.error("Migration execution failed", e);
            lastExecutionStatus = MigrationStatus.FAILED;
            lastExecutionMessage = e.getMessage();
            throw new RuntimeException("Failed to execute migrations", e);
        }
    }

    /**
     * Execute migrations asynchronously (non-blocking startup)
     */
    @Async
    public void executeMigrationsAsync() {
        logger.info("Executing migrations (asynchronous)...");

        try {
            Thread.sleep(1000); // Small delay to let application start
            executeMigrations();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Async migration execution interrupted", e);
        }
    }

    /**
     * Generate baseline from current database schema
     */
    public boolean generateBaseline() {
        String outputPath = "src/main/resources/db/migration/" + properties.getBaselineFileName();

        logger.info("Generating baseline migration at: {}", outputPath);

        boolean success = baselineGeneratorService.generateBaseline(outputPath);

        if (success) {
            logger.info("Baseline generated successfully");
        } else {
            logger.error("Failed to generate baseline");
        }

        return success;
    }

    /**
     * Record migration execution in custom history table
     */
    private void recordMigrationExecution(MigrationInfo[] migrations, MigrationStatus status, long executionTime) {
        try {
            Integer maxOrder = migrationHistoryRepository.findMaxExecutionOrder();
            int currentOrder = (maxOrder != null) ? maxOrder + 1 : 1;

            for (MigrationInfo migration : migrations) {
                if (migration.getVersion() == null) continue;

                MigrationHistory history = new MigrationHistory();
                history.setVersion(migration.getVersion().toString());
                history.setDescription(migration.getDescription());
                history.setType(MigrationType.VERSIONED);
                history.setScript(migration.getScript() != null ? migration.getScript() : migration.getPhysicalLocation());
                history.setChecksum(""); // Would calculate from script content
                history.setExecutionOrder(currentOrder++);
                history.setStatus(status);
                history.setExecutedAt(LocalDateTime.now());
                history.setExecutionTimeMs(executionTime);
                history.setExecutedBy(System.getProperty("user.name"));
                history.setApplicationVersion(applicationName);

                migrationHistoryRepository.save(history);
            }

        } catch (Exception e) {
            logger.error("Failed to record migration execution", e);
        }
    }

    /**
     * Get migration statistics
     */
    public MigrationStatistics getStatistics() {
        MigrationStatistics stats = new MigrationStatistics();

        stats.totalMigrations = migrationHistoryRepository.count();
        stats.successfulMigrations = migrationHistoryRepository.countByStatus(MigrationStatus.SUCCESS);
        stats.failedMigrations = migrationHistoryRepository.countByStatus(MigrationStatus.FAILED);
        stats.pendingMigrations = flyway.info().pending().length;
        stats.lastExecutionStatus = lastExecutionStatus;
        stats.lastExecutionMessage = lastExecutionMessage;

        // Get recent migrations
        stats.recentMigrations = migrationHistoryRepository
                .findRecentMigrations(LocalDateTime.now().minusDays(1));

        return stats;
    }

    /**
     * Migration statistics DTO
     */
    public static class MigrationStatistics {
        public long totalMigrations;
        public long successfulMigrations;
        public long failedMigrations;
        public int pendingMigrations;
        public MigrationStatus lastExecutionStatus;
        public String lastExecutionMessage;
        public List<MigrationHistory> recentMigrations;
    }
}
