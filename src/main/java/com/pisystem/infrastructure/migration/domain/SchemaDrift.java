package com.pisystem.infrastructure.migration.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of schema drift detection comparing database vs expected schema
 */
public class SchemaDrift {

    private boolean hasDrift;
    private List<String> missingTables = new ArrayList<>();
    private List<String> extraTables = new ArrayList<>();
    private List<ColumnMismatch> columnMismatches = new ArrayList<>();
    private List<String> missingIndexes = new ArrayList<>();
    private List<String> extraIndexes = new ArrayList<>();
    private List<String> missingConstraints = new ArrayList<>();
    private List<String> extraConstraints = new ArrayList<>();

    public boolean hasDrift() {
        return hasDrift;
    }

    public void setHasDrift(boolean hasDrift) {
        this.hasDrift = hasDrift;
    }

    public List<String> getMissingTables() {
        return missingTables;
    }

    public void setMissingTables(List<String> missingTables) {
        this.missingTables = missingTables;
    }

    public List<String> getExtraTables() {
        return extraTables;
    }

    public void setExtraTables(List<String> extraTables) {
        this.extraTables = extraTables;
    }

    public List<ColumnMismatch> getColumnMismatches() {
        return columnMismatches;
    }

    public void setColumnMismatches(List<ColumnMismatch> columnMismatches) {
        this.columnMismatches = columnMismatches;
    }

    public List<String> getMissingIndexes() {
        return missingIndexes;
    }

    public void setMissingIndexes(List<String> missingIndexes) {
        this.missingIndexes = missingIndexes;
    }

    public List<String> getExtraIndexes() {
        return extraIndexes;
    }

    public void setExtraIndexes(List<String> extraIndexes) {
        this.extraIndexes = extraIndexes;
    }

    public List<String> getMissingConstraints() {
        return missingConstraints;
    }

    public void setMissingConstraints(List<String> missingConstraints) {
        this.missingConstraints = missingConstraints;
    }

    public List<String> getExtraConstraints() {
        return extraConstraints;
    }

    public void setExtraConstraints(List<String> extraConstraints) {
        this.extraConstraints = extraConstraints;
    }

    /**
     * Column mismatch details
     */
    public static class ColumnMismatch {
        private String tableName;
        private String columnName;
        private String mismatchType; // MISSING, EXTRA, TYPE_MISMATCH, NULLABLE_MISMATCH
        private String expected;
        private String actual;

        public ColumnMismatch(String tableName, String columnName, String mismatchType, String expected, String actual) {
            this.tableName = tableName;
            this.columnName = columnName;
            this.mismatchType = mismatchType;
            this.expected = expected;
            this.actual = actual;
        }

        // Getters
        public String getTableName() { return tableName; }
        public String getColumnName() { return columnName; }
        public String getMismatchType() { return mismatchType; }
        public String getExpected() { return expected; }
        public String getActual() { return actual; }

        @Override
        public String toString() {
            return String.format("%s.%s: %s (expected: %s, actual: %s)",
                    tableName, columnName, mismatchType, expected, actual);
        }
    }

    public void calculateDrift() {
        this.hasDrift = !missingTables.isEmpty() ||
                !extraTables.isEmpty() ||
                !columnMismatches.isEmpty() ||
                !missingIndexes.isEmpty() ||
                !extraIndexes.isEmpty() ||
                !missingConstraints.isEmpty() ||
                !extraConstraints.isEmpty();
    }

    @Override
    public String toString() {
        return "SchemaDrift{" +
                "hasDrift=" + hasDrift +
                ", missingTables=" + missingTables.size() +
                ", extraTables=" + extraTables.size() +
                ", columnMismatches=" + columnMismatches.size() +
                ", missingIndexes=" + missingIndexes.size() +
                ", extraIndexes=" + extraIndexes.size() +
                ", missingConstraints=" + missingConstraints.size() +
                ", extraConstraints=" + extraConstraints.size() +
                '}';
    }
}
