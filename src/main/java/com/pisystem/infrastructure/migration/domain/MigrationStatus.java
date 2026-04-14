package com.pisystem.infrastructure.migration.domain;

/**
 * Execution status of a migration
 */
public enum MigrationStatus {
    /**
     * Migration is pending execution
     */
    PENDING,

    /**
     * Migration is currently executing
     */
    RUNNING,

    /**
     * Migration executed successfully
     */
    SUCCESS,

    /**
     * Migration failed during execution
     */
    FAILED,

    /**
     * Migration was skipped (e.g., out-of-order disabled)
     */
    SKIPPED,

    /**
     * Migration was rolled back
     */
    ROLLED_BACK,

    /**
     * Migration checksum changed - validation failed
     */
    CHECKSUM_MISMATCH
}
