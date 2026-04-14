package com.pisystem.infrastructure.migration.service;

import com.pisystem.infrastructure.migration.config.MigrationProperties;
import com.pisystem.infrastructure.migration.domain.SchemaDrift;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Detects schema drift by comparing current database schema
 * with expected schema from migrations
 */
@Service
public class SchemaDriftDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(SchemaDriftDetectionService.class);

    private final JdbcTemplate jdbcTemplate;
    private final MigrationProperties properties;

    public SchemaDriftDetectionService(JdbcTemplate jdbcTemplate, MigrationProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    /**
     * Detect schema drift between current database and expected schema
     *
     * @param expectedTables List of tables that should exist
     * @return SchemaDrift object with detected differences
     */
    public SchemaDrift detectDrift(Set<String> expectedTables) {
        if (!properties.isDriftDetectionEnabled()) {
            logger.info("Schema drift detection is disabled");
            return new SchemaDrift();
        }

        logger.info("Starting schema drift detection...");

        SchemaDrift drift = new SchemaDrift();

        try {
            String database = getCurrentDatabase();

            // Get actual tables from database
            Set<String> actualTables = new HashSet<>(getAllTables(database));

            logger.info("Expected tables: {}, Actual tables: {}", expectedTables.size(), actualTables.size());

            // Find missing tables
            for (String expected : expectedTables) {
                if (!actualTables.contains(expected)) {
                    drift.getMissingTables().add(expected);
                }
            }

            // Find extra tables
            for (String actual : actualTables) {
                if (!expectedTables.contains(actual)) {
                    drift.getExtraTables().add(actual);
                }
            }

            // Check column structure for common tables
            Set<String> commonTables = new HashSet<>(actualTables);
            commonTables.retainAll(expectedTables);

            for (String table : commonTables) {
                detectColumnDrift(table, database, drift);
                detectIndexDrift(table, database, drift);
            }

            drift.calculateDrift();

            if (drift.hasDrift()) {
                logger.warn("Schema drift detected: {}", drift);
                logger.warn("Missing tables: {}", drift.getMissingTables());
                logger.warn("Extra tables: {}", drift.getExtraTables());
                logger.warn("Column mismatches: {}", drift.getColumnMismatches().size());
            } else {
                logger.info("No schema drift detected");
            }

        } catch (Exception e) {
            logger.error("Failed to detect schema drift", e);
        }

        return drift;
    }

    private void detectColumnDrift(String tableName, String database, SchemaDrift drift) {
        // This is a simplified version - in production, you'd compare against
        // expected column definitions parsed from migration files
        // For now, we just validate column existence

        try {
            String sql = """
                SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = ?
                AND TABLE_NAME = ?
                ORDER BY ORDINAL_POSITION
                """;

            List<Map<String, Object>> columns = jdbcTemplate.queryForList(sql, database, tableName);

            // In a full implementation, you would:
            // 1. Parse expected column structure from migrations
            // 2. Compare with actual columns
            // 3. Detect type mismatches, nullable differences, etc.

            logger.debug("Table {} has {} columns", tableName, columns.size());

        } catch (Exception e) {
            logger.warn("Failed to detect column drift for table: {}", tableName, e);
        }
    }

    private void detectIndexDrift(String tableName, String database, SchemaDrift drift) {
        try {
            String sql = """
                SELECT DISTINCT INDEX_NAME
                FROM information_schema.STATISTICS
                WHERE TABLE_SCHEMA = ?
                AND TABLE_NAME = ?
                AND INDEX_NAME != 'PRIMARY'
                """;

            List<String> indexes = jdbcTemplate.queryForList(sql, database, tableName)
                    .stream()
                    .map(row -> (String) row.get("INDEX_NAME"))
                    .toList();

            logger.debug("Table {} has {} indexes", tableName, indexes.size());

            // In full implementation, compare with expected indexes

        } catch (Exception e) {
            logger.warn("Failed to detect index drift for table: {}", tableName, e);
        }
    }

    private String getCurrentDatabase() {
        return jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
    }

    private List<String> getAllTables(String database) {
        String sql = """
            SELECT TABLE_NAME
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = ?
            AND TABLE_TYPE = 'BASE TABLE'
            """;
        return jdbcTemplate.queryForList(sql, String.class, database);
    }

    /**
     * Get all table names from Flyway migration files
     * This is a simplified version - in production, you'd parse migration files
     */
    public Set<String> getExpectedTablesFromMigrations() {
        // In full implementation:
        // 1. Read all migration files
        // 2. Parse CREATE TABLE statements
        // 3. Build set of expected tables

        // For now, return empty set - actual implementation would parse migration files
        return new HashSet<>();
    }
}
