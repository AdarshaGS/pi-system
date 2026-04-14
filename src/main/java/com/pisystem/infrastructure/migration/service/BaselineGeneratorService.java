package com.pisystem.infrastructure.migration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates baseline SQL script from current database schema.
 * Reads schema metadata and creates CREATE TABLE statements with proper ordering.
 */
@Service
public class BaselineGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(BaselineGeneratorService.class);

    private final JdbcTemplate jdbcTemplate;

    public BaselineGeneratorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Generate baseline SQL file from current database schema
     *
     * @param outputPath Path where to write the baseline file
     * @return true if successful
     */
    public boolean generateBaseline(String outputPath) {
        logger.info("Starting baseline generation to: {}", outputPath);

        try {
            // Get database name
            String databaseName = getCurrentDatabase();
            logger.info("Generating baseline for database: {}", databaseName);

            // Get all tables
            List<String> tables = getAllTables(databaseName);
            logger.info("Found {} tables", tables.size());

            // Order tables by foreign key dependencies
            List<String> orderedTables = orderTablesByDependencies(tables, databaseName);

            // Generate CREATE TABLE statements
            StringBuilder baseline = new StringBuilder();
            baseline.append("-- ============================================================================\n");
            baseline.append("-- BASELINE MIGRATION - Generated from database schema\n");
            baseline.append(String.format("-- Database: %s\n", databaseName));
            baseline.append(String.format("-- Generated: %s\n", java.time.LocalDateTime.now()));
            baseline.append("-- ============================================================================\n\n");

            for (String table : orderedTables) {
                String createTableDdl = generateCreateTable(table, databaseName);
                baseline.append(createTableDdl);
                baseline.append("\n\n");

                // Add indexes
                String indexesDdl = generateIndexes(table, databaseName);
                if (!indexesDdl.isEmpty()) {
                    baseline.append(indexesDdl);
                    baseline.append("\n\n");
                }
            }

            // Add foreign keys (after all tables are created)
            baseline.append("-- ============================================================================\n");
            baseline.append("-- FOREIGN KEY CONSTRAINTS\n");
            baseline.append("-- ============================================================================\n\n");

            for (String table : orderedTables) {
                String foreignKeysDdl = generateForeignKeys(table, databaseName);
                if (!foreignKeysDdl.isEmpty()) {
                    baseline.append(foreignKeysDdl);
                    baseline.append("\n");
                }
            }

            // Write to file
            Path path = Paths.get(outputPath);
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write(baseline.toString());
            }

            logger.info("Baseline generated successfully at: {}", outputPath);
            return true;

        } catch (Exception e) {
            logger.error("Failed to generate baseline", e);
            return false;
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
            ORDER BY TABLE_NAME
            """;
        return jdbcTemplate.queryForList(sql, String.class, database);
    }

    private List<String> orderTablesByDependencies(List<String> tables, String database) {
        // Build dependency graph
        Map<String, Set<String>> dependencies = new HashMap<>();

        for (String table : tables) {
            String sql = """
                SELECT REFERENCED_TABLE_NAME
                FROM information_schema.KEY_COLUMN_USAGE
                WHERE TABLE_SCHEMA = ?
                AND TABLE_NAME = ?
                AND REFERENCED_TABLE_NAME IS NOT NULL
                """;

            List<String> deps = jdbcTemplate.queryForList(sql, String.class, database, table);
            dependencies.put(table, new HashSet<>(deps));
        }

        // Topological sort
        List<String> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();

        for (String table : tables) {
            topologicalSort(table, dependencies, visited, visiting, ordered);
        }

        return ordered;
    }

    private void topologicalSort(String table, Map<String, Set<String>> dependencies,
                                  Set<String> visited, Set<String> visiting, List<String> ordered) {
        if (visited.contains(table)) {
            return;
        }

        if (visiting.contains(table)) {
            // Circular dependency - just add it
            logger.warn("Circular dependency detected involving table: {}", table);
            return;
        }

        visiting.add(table);

        Set<String> deps = dependencies.get(table);
        if (deps != null) {
            for (String dep : deps) {
                if (dependencies.containsKey(dep)) {
                    topologicalSort(dep, dependencies, visited, visiting, ordered);
                }
            }
        }

        visiting.remove(table);
        visited.add(table);
        ordered.add(table);
    }

    private String generateCreateTable(String tableName, String database) {
        StringBuilder ddl = new StringBuilder();

        ddl.append(String.format("-- Table: %s\n", tableName));
        ddl.append(String.format("CREATE TABLE IF NOT EXISTS `%s` (\n", tableName));

        // Get columns
        String columnsSql = """
            SELECT
                COLUMN_NAME,
                COLUMN_TYPE,
                IS_NULLABLE,
                COLUMN_DEFAULT,
                EXTRA,
                COLUMN_COMMENT
            FROM information_schema.COLUMNS
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            ORDER BY ORDINAL_POSITION
            """;

        List<Map<String, Object>> columns = jdbcTemplate.queryForList(columnsSql, database, tableName);

        List<String> columnDefs = new ArrayList<>();
        for (Map<String, Object> column : columns) {
            String columnDef = generateColumnDefinition(column);
            columnDefs.add("    " + columnDef);
        }

        // Add primary key
        String pkConstraint = getPrimaryKeyConstraint(tableName, database);
        if (pkConstraint != null) {
            columnDefs.add("    " + pkConstraint);
        }

        ddl.append(String.join(",\n", columnDefs));
        ddl.append("\n");

        // Add table options
        String tableOptions = getTableOptions(tableName, database);
        ddl.append(") ").append(tableOptions).append(";");

        return ddl.toString();
    }

    private String generateColumnDefinition(Map<String, Object> column) {
        StringBuilder def = new StringBuilder();

        String columnName = (String) column.get("COLUMN_NAME");
        String columnType = (String) column.get("COLUMN_TYPE");
        String isNullable = (String) column.get("IS_NULLABLE");
        Object defaultValue = column.get("COLUMN_DEFAULT");
        String extra = (String) column.get("EXTRA");
        String comment = (String) column.get("COLUMN_COMMENT");

        def.append("`").append(columnName).append("` ");
        def.append(columnType);

        if ("NO".equals(isNullable)) {
            def.append(" NOT NULL");
        }

        if (defaultValue != null) {
            if ("CURRENT_TIMESTAMP".equals(defaultValue)) {
                def.append(" DEFAULT CURRENT_TIMESTAMP");
            } else {
                def.append(" DEFAULT '").append(defaultValue).append("'");
            }
        }

        if (extra != null && !extra.isEmpty()) {
            def.append(" ").append(extra);
        }

        if (comment != null && !comment.isEmpty()) {
            def.append(" COMMENT '").append(comment.replace("'", "''")).append("'");
        }

        return def.toString();
    }

    private String getPrimaryKeyConstraint(String tableName, String database) {
        String sql = """
            SELECT COLUMN_NAME
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            AND CONSTRAINT_NAME = 'PRIMARY'
            ORDER BY ORDINAL_POSITION
            """;

        List<String> pkColumns = jdbcTemplate.queryForList(sql, String.class, database, tableName);

        if (pkColumns.isEmpty()) {
            return null;
        }

        String columns = pkColumns.stream()
                .map(col -> "`" + col + "`")
                .collect(Collectors.joining(", "));

        return "PRIMARY KEY (" + columns + ")";
    }

    private String getTableOptions(String tableName, String database) {
        String sql = """
            SELECT ENGINE, TABLE_COLLATION, TABLE_COMMENT
            FROM information_schema.TABLES
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            """;

        Map<String, Object> options = jdbcTemplate.queryForMap(sql, database, tableName);

        StringBuilder opts = new StringBuilder();
        String engine = (String) options.get("ENGINE");
        String collation = (String) options.get("TABLE_COLLATION");
        String comment = (String) options.get("TABLE_COMMENT");

        if (engine != null) {
            opts.append("ENGINE=").append(engine);
        }

        if (collation != null) {
            if (opts.length() > 0) opts.append(" ");
            String charset = collation.split("_")[0];
            opts.append("DEFAULT CHARSET=").append(charset);
            opts.append(" COLLATE=").append(collation);
        }

        if (comment != null && !comment.isEmpty()) {
            if (opts.length() > 0) opts.append(" ");
            opts.append("COMMENT='").append(comment.replace("'", "''")).append("'");
        }

        return opts.toString();
    }

    private String generateIndexes(String tableName, String database) {
        String sql = """
            SELECT DISTINCT
                INDEX_NAME,
                NON_UNIQUE,
                GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX SEPARATOR ', ') as COLUMNS
            FROM information_schema.STATISTICS
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            AND INDEX_NAME != 'PRIMARY'
            GROUP BY INDEX_NAME, NON_UNIQUE
            ORDER BY INDEX_NAME
            """;

        List<Map<String, Object>> indexes = jdbcTemplate.queryForList(sql, database, tableName);

        if (indexes.isEmpty()) {
            return "";
        }

        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("-- Indexes for table: %s\n", tableName));

        for (Map<String, Object> index : indexes) {
            String indexName = (String) index.get("INDEX_NAME");
            Integer nonUnique = ((Number) index.get("NON_UNIQUE")).intValue();
            String columns = (String) index.get("COLUMNS");

            if (nonUnique == 0) {
                ddl.append(String.format("CREATE UNIQUE INDEX `%s` ON `%s` (%s);\n",
                        indexName, tableName, columns));
            } else {
                ddl.append(String.format("CREATE INDEX `%s` ON `%s` (%s);\n",
                        indexName, tableName, columns));
            }
        }

        return ddl.toString();
    }

    private String generateForeignKeys(String tableName, String database) {
        String sql = """
            SELECT
                CONSTRAINT_NAME,
                COLUMN_NAME,
                REFERENCED_TABLE_NAME,
                REFERENCED_COLUMN_NAME,
                DELETE_RULE,
                UPDATE_RULE
            FROM information_schema.KEY_COLUMN_USAGE
            WHERE TABLE_SCHEMA = ?
            AND TABLE_NAME = ?
            AND REFERENCED_TABLE_NAME IS NOT NULL
            ORDER BY CONSTRAINT_NAME, ORDINAL_POSITION
            """;

        List<Map<String, Object>> foreignKeys = jdbcTemplate.queryForList(sql, database, tableName);

        if (foreignKeys.isEmpty()) {
            return "";
        }

        StringBuilder ddl = new StringBuilder();

        // Group by constraint name
        Map<String, List<Map<String, Object>>> grouped = foreignKeys.stream()
                .collect(Collectors.groupingBy(fk -> (String) fk.get("CONSTRAINT_NAME")));

        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            String constraintName = entry.getKey();
            List<Map<String, Object>> fkColumns = entry.getValue();

            Map<String, Object> first = fkColumns.get(0);
            String refTable = (String) first.get("REFERENCED_TABLE_NAME");

            String columns = fkColumns.stream()
                    .map(fk -> "`" + fk.get("COLUMN_NAME") + "`")
                    .collect(Collectors.joining(", "));

            String refColumns = fkColumns.stream()
                    .map(fk -> "`" + fk.get("REFERENCED_COLUMN_NAME") + "`")
                    .collect(Collectors.joining(", "));

            String deleteRule = (String) first.get("DELETE_RULE");
            String updateRule = (String) first.get("UPDATE_RULE");

            ddl.append(String.format("ALTER TABLE `%s` ADD CONSTRAINT `%s` FOREIGN KEY (%s) REFERENCES `%s` (%s)",
                    tableName, constraintName, columns, refTable, refColumns));

            if (!"RESTRICT".equals(deleteRule)) {
                ddl.append(" ON DELETE ").append(deleteRule);
            }

            if (!"RESTRICT".equals(updateRule)) {
                ddl.append(" ON UPDATE ").append(updateRule);
            }

            ddl.append(";\n");
        }

        return ddl.toString();
    }
}
