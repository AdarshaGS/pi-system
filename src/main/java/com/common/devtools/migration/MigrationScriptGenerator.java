package com.common.devtools.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates SQL migration scripts from schema changes
 */
@Component
@Slf4j
public class MigrationScriptGenerator {

    /**
     * Generate Flyway migration SQL from schema changes
     */
    public String generateMigrationScript(SchemaDrift drift, String description) {
        if (!drift.isHasChanges()) {
            return "-- No schema changes detected\n";
        }

        StringBuilder sql = new StringBuilder();
        sql.append("-- Migration: ").append(description).append("\n");
        sql.append("-- Generated: ").append(java.time.LocalDateTime.now()).append("\n");
        sql.append("-- Changes: ").append(drift.getChangeCount()).append("\n\n");

        // Group changes by type
        List<SchemaChange> createTables = filterByType(drift.getChanges(), SchemaChangeType.CREATE_TABLE);
        List<SchemaChange> dropTables = filterByType(drift.getChanges(), SchemaChangeType.DROP_TABLE);
        List<SchemaChange> addColumns = filterByType(drift.getChanges(), SchemaChangeType.ADD_COLUMN);
        List<SchemaChange> dropColumns = filterByType(drift.getChanges(), SchemaChangeType.DROP_COLUMN);
        List<SchemaChange> modifyColumns = filterByType(drift.getChanges(), SchemaChangeType.MODIFY_COLUMN);
        List<SchemaChange> addIndexes = filterByType(drift.getChanges(), SchemaChangeType.ADD_INDEX);
        List<SchemaChange> dropIndexes = filterByType(drift.getChanges(), SchemaChangeType.DROP_INDEX);

        // Generate SQL for each type
        if (!createTables.isEmpty()) {
            sql.append("-- Create new tables\n");
            for (SchemaChange change : createTables) {
                sql.append(generateCreateTable(change.getNewSchema()));
                sql.append("\n");
            }
        }

        if (!addColumns.isEmpty()) {
            sql.append("-- Add new columns\n");
            for (SchemaChange change : addColumns) {
                sql.append(generateAddColumn(change.getTableName(), change.getNewColumn()));
                sql.append("\n");
            }
        }

        if (!modifyColumns.isEmpty()) {
            sql.append("-- Modify existing columns\n");
            for (SchemaChange change : modifyColumns) {
                sql.append(generateModifyColumn(change.getTableName(), change.getNewColumn()));
                sql.append("\n");
            }
        }

        if (!addIndexes.isEmpty()) {
            sql.append("-- Add indexes\n");
            for (SchemaChange change : addIndexes) {
                sql.append(generateCreateIndex(change.getTableName(), change.getNewIndex()));
                sql.append("\n");
            }
        }

        if (!dropIndexes.isEmpty()) {
            sql.append("-- Drop indexes\n");
            for (SchemaChange change : dropIndexes) {
                sql.append(generateDropIndex(change.getTableName(), change.getOldIndex()));
                sql.append("\n");
            }
        }

        if (!dropColumns.isEmpty()) {
            sql.append("-- Drop columns (handle with care!)\n");
            for (SchemaChange change : dropColumns) {
                sql.append(generateDropColumn(change.getTableName(), change.getOldColumn()));
                sql.append("\n");
            }
        }

        if (!dropTables.isEmpty()) {
            sql.append("-- Drop tables (handle with care!)\n");
            for (SchemaChange change : dropTables) {
                sql.append(generateDropTable(change.getTableName()));
                sql.append("\n");
            }
        }

        return sql.toString();
    }

    private List<SchemaChange> filterByType(List<SchemaChange> changes, SchemaChangeType type) {
        return changes.stream()
                .filter(c -> c.getType() == type)
                .collect(Collectors.toList());
    }

    private String generateCreateTable(EntitySchema schema) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(schema.getTableName()).append(" (\n");

        List<String> columnDefs = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();

        for (ColumnSchema column : schema.getColumns()) {
            StringBuilder colDef = new StringBuilder();
            colDef.append("    ").append(column.getColumnName()).append(" ").append(column.getSqlType());

            if (column.isAutoIncrement()) {
                colDef.append(" AUTO_INCREMENT");
            }

            if (!column.isNullable()) {
                colDef.append(" NOT NULL");
            }

            if (column.getDefaultValue() != null) {
                colDef.append(" DEFAULT ").append(column.getDefaultValue());
            }

            columnDefs.add(colDef.toString());

            if (column.isPrimaryKey()) {
                primaryKeys.add(column.getColumnName());
            }
        }

        sql.append(String.join(",\n", columnDefs));

        if (!primaryKeys.isEmpty()) {
            sql.append(",\n    PRIMARY KEY (").append(String.join(", ", primaryKeys)).append(")");
        }

        sql.append("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='")
           .append(schema.getClassName()).append("';\n");

        // Add indexes
        for (IndexSchema index : schema.getIndexes()) {
            sql.append(generateCreateIndex(schema.getTableName(), index));
        }

        // Add unique constraints
        for (UniqueConstraintSchema constraint : schema.getUniqueConstraints()) {
            sql.append(generateUniqueConstraint(schema.getTableName(), constraint));
        }

        return sql.toString();
    }

    private String generateAddColumn(String tableName, ColumnSchema column) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName)
           .append(" ADD COLUMN ").append(column.getColumnName())
           .append(" ").append(column.getSqlType());

        if (column.isAutoIncrement()) {
            sql.append(" AUTO_INCREMENT");
        }

        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }

        if (column.getDefaultValue() != null) {
            sql.append(" DEFAULT ").append(column.getDefaultValue());
        }

        sql.append(";\n");
        return sql.toString();
    }

    private String generateModifyColumn(String tableName, ColumnSchema column) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName)
           .append(" MODIFY COLUMN ").append(column.getColumnName())
           .append(" ").append(column.getSqlType());

        if (column.isAutoIncrement()) {
            sql.append(" AUTO_INCREMENT");
        }

        if (!column.isNullable()) {
            sql.append(" NOT NULL");
        }

        if (column.getDefaultValue() != null) {
            sql.append(" DEFAULT ").append(column.getDefaultValue());
        }

        sql.append(";\n");
        return sql.toString();
    }

    private String generateDropColumn(String tableName, ColumnSchema column) {
        return "-- ALTER TABLE " + tableName + " DROP COLUMN " + column.getColumnName() + ";\n";
    }

    private String generateCreateIndex(String tableName, IndexSchema index) {
        StringBuilder sql = new StringBuilder();
        if (index.isUnique()) {
            sql.append("CREATE UNIQUE INDEX ");
        } else {
            sql.append("CREATE INDEX ");
        }
        sql.append(index.getName()).append(" ON ").append(tableName)
           .append(" (").append(index.getColumnList()).append(");\n");
        return sql.toString();
    }

    private String generateDropIndex(String tableName, IndexSchema index) {
        return "-- DROP INDEX " + index.getName() + " ON " + tableName + ";\n";
    }

    private String generateDropTable(String tableName) {
        return "-- DROP TABLE IF EXISTS " + tableName + ";\n";
    }

    private String generateUniqueConstraint(String tableName, UniqueConstraintSchema constraint) {
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(tableName)
           .append(" ADD CONSTRAINT ").append(constraint.getName())
           .append(" UNIQUE (").append(String.join(", ", constraint.getColumnNames()))
           .append(");\n");
        return sql.toString();
    }
}
