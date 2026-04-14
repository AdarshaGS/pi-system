package com.pisystem.infrastructure.migration.service;

import com.pisystem.infrastructure.migration.config.MigrationProperties;
import com.pisystem.infrastructure.migration.domain.MigrationHistory;
import com.pisystem.infrastructure.migration.domain.MigrationStatus;
import com.pisystem.infrastructure.migration.repository.MigrationHistoryRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validates migrations before application startup
 * - Checks all migrations are present
 * - Detects checksum mismatches
 * - Detects out-of-order execution
 */
@Service
public class MigrationValidationService {

    private static final Logger logger = LoggerFactory.getLogger(MigrationValidationService.class);

    private final Flyway flyway;
    private final MigrationHistoryRepository migrationHistoryRepository;
    private final MigrationProperties properties;

    @Autowired
    public MigrationValidationService(Flyway flyway,
                                      MigrationHistoryRepository migrationHistoryRepository,
                                      MigrationProperties properties) {
        this.flyway = flyway;
        this.migrationHistoryRepository = migrationHistoryRepository;
        this.properties = properties;
    }

    /**
     * Validates all migrations
     *
     * @return List of validation errors (empty if valid)
     */
    public List<String> validateMigrations() {
        List<String> errors = new ArrayList<>();

        if (!properties.isValidationEnabled()) {
            logger.info("Migration validation is disabled");
            return errors;
        }

        logger.info("Starting migration validation...");

        try {
            MigrationInfoService infoService = flyway.info();
            MigrationInfo[] allMigrations = infoService.all();

            MigrationInfo[] pending = infoService.pending();
            if (pending.length > 0) {
                logger.warn("Found {} pending migrations", pending.length);
                for (MigrationInfo migration : pending) {
                    logger.warn("  - {} : {}", migration.getVersion(), migration.getDescription());
                }
            }

            for (MigrationInfo migration : allMigrations) {
                if (migration.getState() != null && 
                    migration.getState().name().equals("FAILED")) {
                    String error = String.format("Failed migration: %s - %s",
                            migration.getVersion(), migration.getDescription());
                    errors.add(error);
                    logger.error(error);
                }
            }

            errors.addAll(validateChecksums(allMigrations));

            errors.addAll(validateExecutionOrder(allMigrations));

            errors.addAll(validateNoMissingMigrations(allMigrations));

            if (errors.isEmpty()) {
                logger.info("Migration validation completed successfully - no errors found");
            } else {
                logger.error("Migration validation found {} errors", errors.size());
                errors.forEach(logger::error);
            }

        } catch (Exception e) {
            String error = "Migration validation failed with exception: " + e.getMessage();
            errors.add(error);
            logger.error(error, e);
        }

        return errors;
    }

    private List<String> validateChecksums(MigrationInfo[] migrations) {
        List<String> errors = new ArrayList<>();

        try {
            for (MigrationInfo migration : migrations) {
                if (migration.getState() == null) continue;

                String version = migration.getVersion() != null ? migration.getVersion().toString() : null;
                if (version == null) continue;

                migrationHistoryRepository.findByVersion(version).ifPresent(history -> {
                    String currentChecksum = calculateChecksum(migration.getScript());
                    if (!currentChecksum.equals(history.getChecksum())) {
                        String error = String.format(
                                "Checksum mismatch for migration %s: expected %s, got %s",
                                version, history.getChecksum(), currentChecksum);
                        errors.add(error);

                        // Update status to CHECKSUM_MISMATCH
                        history.setStatus(MigrationStatus.CHECKSUM_MISMATCH);
                        migrationHistoryRepository.save(history);
                    }
                });
            }
        } catch (Exception e) {
            // Table might not exist yet - this is OK on first run
            logger.debug("Could not validate checksums (migration_history table may not exist yet): {}", e.getMessage());
        }

        return errors;
    }

    private List<String> validateExecutionOrder(MigrationInfo[] migrations) {
        List<String> errors = new ArrayList<>();

        // Check if migrations are in correct order
        String previousVersion = null;
        for (MigrationInfo migration : migrations) {
            String version = migration.getVersion() != null ? migration.getVersion().toString() : null;
            if (version != null && previousVersion != null) {
                if (version.compareTo(previousVersion) < 0) {
                    String error = String.format(
                            "Out-of-order migration detected: %s comes after %s",
                            version, previousVersion);
                    errors.add(error);
                }
            }
            previousVersion = version;
        }

        return errors;
    }

    private List<String> validateNoMissingMigrations(MigrationInfo[] migrations) {
        List<String> errors = new ArrayList<>();

        try {
            // Get all successful migrations from history
            List<MigrationHistory> successful = migrationHistoryRepository.findAllSuccessfulMigrations();

            // Check if any are missing from current migration files
            for (MigrationHistory history : successful) {
                boolean found = false;
                for (MigrationInfo migration : migrations) {
                    String version = migration.getVersion() != null ? migration.getVersion().toString() : null;
                    if (history.getVersion().equals(version)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    String error = String.format(
                            "Previously executed migration is missing: %s - %s",
                            history.getVersion(), history.getDescription());
                    errors.add(error);
                }
            }
        } catch (Exception e) {
            // Table might not exist yet - this is OK on first run
            logger.debug("Could not validate missing migrations (migration_history table may not exist yet): {}", e.getMessage());
        }

        return errors;
    }

    /**
     * Calculate SHA-256 checksum of migration script
     */
    private String calculateChecksum(String script) {
        if (script == null) return "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(script.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Failed to calculate checksum", e);
            return "";
        }
    }

    /**
     * Check if destructive operations are present in migration
     */
    public boolean containsDestructiveOperations(String script) {
        if (script == null) return false;

        String upperScript = script.toUpperCase();
        return upperScript.contains("DROP TABLE") ||
                upperScript.contains("DROP COLUMN") ||
                upperScript.contains("TRUNCATE") ||
                upperScript.contains("DELETE FROM");
    }

    /**
     * Validate backward compatibility (expand-migrate-contract pattern)
     */
    public List<String> validateBackwardCompatibility(String script) {
        List<String> warnings = new ArrayList<>();

        if (!properties.isBackwardCompatibilityChecks()) {
            return warnings;
        }

        // Check for immediate column drops (should be in separate migration)
        if (script.toUpperCase().contains("DROP COLUMN")) {
            warnings.add("Dropping column immediately may break backward compatibility. " +
                    "Consider: 1) Add new column, 2) Migrate data, 3) Drop old column in later migration");
        }

        // Check for column renames
        if (script.toUpperCase().contains("RENAME COLUMN")) {
            warnings.add("Renaming column may break backward compatibility. " +
                    "Consider: 1) Add new column, 2) Migrate data, 3) Drop old column later");
        }

        return warnings;
    }
}
