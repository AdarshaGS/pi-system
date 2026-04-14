package com.pisystem.infrastructure.migration.controller;

import com.pisystem.infrastructure.migration.config.MigrationProperties;
import com.pisystem.infrastructure.migration.domain.MigrationHistory;
import com.pisystem.infrastructure.migration.domain.SchemaDrift;
import com.pisystem.infrastructure.migration.repository.MigrationHistoryRepository;
import com.pisystem.infrastructure.migration.service.MigrationManagerService;
import com.pisystem.infrastructure.migration.service.MigrationValidationService;
import com.pisystem.infrastructure.migration.service.SchemaDriftDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST endpoint for migration management and monitoring
 * Exposed via Spring Boot Actuator pattern
 */
@RestController
@RequestMapping("/actuator/migrations")
@Tag(name = "Migration Management", description = "Database migration monitoring and management")
public class MigrationActuatorController {

    private static final Logger logger = LoggerFactory.getLogger(MigrationActuatorController.class);

    private final MigrationManagerService migrationManager;
    private final MigrationHistoryRepository migrationHistoryRepository;
    private final MigrationValidationService validationService;
    private final SchemaDriftDetectionService driftDetectionService;
    private final MigrationProperties properties;

    @Autowired
    public MigrationActuatorController(
            MigrationManagerService migrationManager,
            MigrationHistoryRepository migrationHistoryRepository,
            MigrationValidationService validationService,
            SchemaDriftDetectionService driftDetectionService,
            MigrationProperties properties) {
        this.migrationManager = migrationManager;
        this.migrationHistoryRepository = migrationHistoryRepository;
        this.validationService = validationService;
        this.driftDetectionService = driftDetectionService;
        this.properties = properties;
    }

    /**
     * Get migration status and statistics
     */
    @GetMapping("/status")
    @Operation(summary = "Get migration status", description = "Returns current migration system status and statistics")
    public ResponseEntity<Map<String, Object>> getStatus() {
        logger.debug("Getting migration status");

        MigrationManagerService.MigrationStatistics stats = migrationManager.getStatistics();

        Map<String, Object> status = new HashMap<>();
        status.put("enabled", properties.isValidationEnabled());
        status.put("totalMigrations", stats.totalMigrations);
        status.put("successfulMigrations", stats.successfulMigrations);
        status.put("failedMigrations", stats.failedMigrations);
        status.put("pendingMigrations", stats.pendingMigrations);
        status.put("lastExecutionStatus", stats.lastExecutionStatus);
        status.put("lastExecutionMessage", stats.lastExecutionMessage);

        return ResponseEntity.ok(status);
    }

    /**
     * Get migration history
     */
    @GetMapping("/history")
    @Operation(summary = "Get migration history", description = "Returns all executed migrations")
    public ResponseEntity<List<MigrationHistory>> getHistory(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status) {

        List<MigrationHistory> history;

        if (module != null) {
            history = migrationHistoryRepository.findByModuleOrderByExecutionOrderAsc(module);
        } else if (status != null) {
            history = migrationHistoryRepository.findByStatusOrderByExecutionOrderAsc(
                    com.pisystem.infrastructure.migration.domain.MigrationStatus.valueOf(status));
        } else {
            history = migrationHistoryRepository.findAll();
        }

        return ResponseEntity.ok(history);
    }

    /**
     * Get recent migrations (last 24 hours)
     */
    @GetMapping("/recent")
    @Operation(summary = "Get recent migrations", description = "Returns migrations executed in the last 24 hours")
    public ResponseEntity<List<MigrationHistory>> getRecent() {
        MigrationManagerService.MigrationStatistics stats = migrationManager.getStatistics();
        return ResponseEntity.ok(stats.recentMigrations);
    }

    /**
     * Validate migrations
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate migrations", description = "Validates all migrations and returns errors if any")
    public ResponseEntity<Map<String, Object>> validate() {
        logger.info("Manual migration validation triggered");

        List<String> errors = validationService.validateMigrations();

        Map<String, Object> result = new HashMap<>();
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("errorCount", errors.size());

        return ResponseEntity.ok(result);
    }

    /**
     * Detect schema drift
     */
    @GetMapping("/drift")
    @Operation(summary = "Detect schema drift", description = "Compares current schema with expected schema")
    public ResponseEntity<SchemaDrift> detectDrift() {
        logger.info("Manual schema drift detection triggered");

        Set<String> expectedTables = driftDetectionService.getExpectedTablesFromMigrations();
        SchemaDrift drift = driftDetectionService.detectDrift(expectedTables);

        return ResponseEntity.ok(drift);
    }

    /**
     * Generate baseline
     */
    @PostMapping("/baseline")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate baseline", description = "Generates baseline SQL from current database schema")
    public ResponseEntity<Map<String, Object>> generateBaseline() {
        logger.info("Manual baseline generation triggered");

        boolean success = migrationManager.generateBaseline();

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", success ? "Baseline generated successfully" : "Failed to generate baseline");

        return ResponseEntity.ok(result);
    }

    /**
     * Get configuration
     */
    @GetMapping("/config")
    @Operation(summary = "Get migration configuration", description = "Returns current migration system configuration")
    public ResponseEntity<MigrationProperties> getConfig() {
        return ResponseEntity.ok(properties);
    }

    /**
     * Health check for migration system
     */
    @GetMapping("/health")
    @Operation(summary = "Migration health check", description = "Returns health status of migration system")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();

        try {
            List<String> validationErrors = validationService.validateMigrations();
            boolean isHealthy = validationErrors.isEmpty();

            health.put("status", isHealthy ? "UP" : "DEGRADED");
            health.put("validationErrors", validationErrors.size());

            if (!isHealthy) {
                health.put("errors", validationErrors);
            }

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
        }

        return ResponseEntity.ok(health);
    }
}
