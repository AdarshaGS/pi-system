package com.pisystem.infrastructure.migration.domain;

/**
 * Type of database migration
 */
public enum MigrationType {
    /**
     * Versioned migration (V__*.sql)
     */
    VERSIONED,

    /**
     * Repeatable migration (R__*.sql) - views, procedures, functions
     */
    REPEATABLE,

    /**
     * Baseline migration - initial schema dump
     */
    BASELINE,

    /**
     * Data migration - seed data, reference data
     */
    DATA,

    /**
     * Hotfix migration - urgent production fixes
     */
    HOTFIX
}
