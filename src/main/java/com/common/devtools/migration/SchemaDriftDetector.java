package com.common.devtools.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Compares two schema states and detects differences (drift)
 */
@Component
@Slf4j
public class SchemaDriftDetector {

    /**
     * Compare two sets of entity schemas and detect differences
     */
    public SchemaDrift detectDrift(List<EntitySchema> currentSchema, List<EntitySchema> targetSchema) {
        List<SchemaChange> changes = new ArrayList<>();

        // Find new tables
        for (EntitySchema target : targetSchema) {
            if (currentSchema.stream().noneMatch(c -> c.getTableName().equals(target.getTableName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.CREATE_TABLE)
                        .tableName(target.getTableName())
                        .description("New table: " + target.getTableName())
                        .newSchema(target)
                        .build());
            }
        }

        // Find dropped tables
        for (EntitySchema current : currentSchema) {
            if (targetSchema.stream().noneMatch(t -> t.getTableName().equals(current.getTableName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.DROP_TABLE)
                        .tableName(current.getTableName())
                        .description("Dropped table: " + current.getTableName())
                        .oldSchema(current)
                        .build());
            }
        }

        // Find modified tables
        for (EntitySchema target : targetSchema) {
            EntitySchema current = currentSchema.stream()
                    .filter(c -> c.getTableName().equals(target.getTableName()))
                    .findFirst()
                    .orElse(null);

            if (current != null) {
                changes.addAll(detectTableChanges(current, target));
            }
        }

        return SchemaDrift.builder()
                .changes(changes)
                .hasChanges(!changes.isEmpty())
                .changeCount(changes.size())
                .build();
    }

    private List<SchemaChange> detectTableChanges(EntitySchema current, EntitySchema target) {
        List<SchemaChange> changes = new ArrayList<>();
        String tableName = current.getTableName();

        // Find new columns
        for (ColumnSchema targetCol : target.getColumns()) {
            if (current.getColumns().stream().noneMatch(c -> c.getColumnName().equals(targetCol.getColumnName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.ADD_COLUMN)
                        .tableName(tableName)
                        .columnName(targetCol.getColumnName())
                        .description("Add column: " + tableName + "." + targetCol.getColumnName())
                        .newColumn(targetCol)
                        .build());
            }
        }

        // Find dropped columns
        for (ColumnSchema currentCol : current.getColumns()) {
            if (target.getColumns().stream().noneMatch(t -> t.getColumnName().equals(currentCol.getColumnName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.DROP_COLUMN)
                        .tableName(tableName)
                        .columnName(currentCol.getColumnName())
                        .description("Drop column: " + tableName + "." + currentCol.getColumnName())
                        .oldColumn(currentCol)
                        .build());
            }
        }

        // Find modified columns
        for (ColumnSchema targetCol : target.getColumns()) {
            ColumnSchema currentCol = current.getColumns().stream()
                    .filter(c -> c.getColumnName().equals(targetCol.getColumnName()))
                    .findFirst()
                    .orElse(null);

            if (currentCol != null && !columnsEqual(currentCol, targetCol)) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.MODIFY_COLUMN)
                        .tableName(tableName)
                        .columnName(currentCol.getColumnName())
                        .description("Modify column: " + tableName + "." + currentCol.getColumnName() + 
                                   " (" + currentCol.getSqlType() + " -> " + targetCol.getSqlType() + ")")
                        .oldColumn(currentCol)
                        .newColumn(targetCol)
                        .build());
            }
        }

        // Find index changes
        changes.addAll(detectIndexChanges(current, target, tableName));

        return changes;
    }

    private boolean columnsEqual(ColumnSchema col1, ColumnSchema col2) {
        return col1.getSqlType().equals(col2.getSqlType()) &&
               col1.isNullable() == col2.isNullable() &&
               col1.isPrimaryKey() == col2.isPrimaryKey() &&
               col1.isAutoIncrement() == col2.isAutoIncrement() &&
               equalOrNull(col1.getDefaultValue(), col2.getDefaultValue()) &&
               equalOrNull(col1.getLength(), col2.getLength()) &&
               equalOrNull(col1.getPrecision(), col2.getPrecision()) &&
               equalOrNull(col1.getScale(), col2.getScale());
    }

    private boolean equalOrNull(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) return true;
        if (obj1 == null || obj2 == null) return false;
        return obj1.equals(obj2);
    }

    private List<SchemaChange> detectIndexChanges(EntitySchema current, EntitySchema target, String tableName) {
        List<SchemaChange> changes = new ArrayList<>();

        // Find new indexes
        for (IndexSchema targetIdx : target.getIndexes()) {
            if (current.getIndexes().stream().noneMatch(i -> i.getName().equals(targetIdx.getName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.ADD_INDEX)
                        .tableName(tableName)
                        .indexName(targetIdx.getName())
                        .description("Add index: " + targetIdx.getName() + " on " + tableName)
                        .newIndex(targetIdx)
                        .build());
            }
        }

        // Find dropped indexes
        for (IndexSchema currentIdx : current.getIndexes()) {
            if (target.getIndexes().stream().noneMatch(i -> i.getName().equals(currentIdx.getName()))) {
                changes.add(SchemaChange.builder()
                        .type(SchemaChangeType.DROP_INDEX)
                        .tableName(tableName)
                        .indexName(currentIdx.getName())
                        .description("Drop index: " + currentIdx.getName() + " on " + tableName)
                        .oldIndex(currentIdx)
                        .build());
            }
        }

        return changes;
    }
}
