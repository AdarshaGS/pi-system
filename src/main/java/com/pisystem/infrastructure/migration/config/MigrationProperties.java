package com.pisystem.infrastructure.migration.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for the hybrid migration system
 */
@Configuration
@ConfigurationProperties(prefix = "migration")
public class MigrationProperties {

    /**
     * Enable/disable baseline generation
     */
    private boolean baselineEnabled = false;

    /**
     * Enable/disable migration validation on startup
     */
    private boolean validationEnabled = true;

    /**
     * Enable/disable schema drift detection
     */
    private boolean driftDetectionEnabled = true;

    /**
     * Fail application startup on validation errors
     */
    private boolean failOnValidationError = true;

    /**
     * Fail application startup on schema drift
     */
    private boolean failOnDrift = false;

    /**
     * Enable/disable modular migration support
     */
    private boolean modularEnabled = true;

    /**
     * List of modules to scan for migrations
     */
    private List<String> modules = new ArrayList<>();

    /**
     * Run migrations asynchronously after startup
     */
    private boolean asyncExecution = false;

    /**
     * Warn about long-running migrations (threshold in seconds)
     */
    private long longRunningThresholdSeconds = 30;

    /**
     * Enable backward compatibility checks (expand-migrate-contract validation)
     */
    private boolean backwardCompatibilityChecks = true;

    /**
     * Allow destructive operations (DROP COLUMN, DROP TABLE)
     */
    private boolean allowDestructiveOperations = false;

    /**
     * Archive old migrations older than N days
     */
    private int archiveAfterDays = 365;

    /**
     * Enable migration archival
     */
    private boolean archivalEnabled = false;

    /**
     * Baseline file name
     */
    private String baselineFileName = "V1__baseline.sql";

    /**
     * Archive directory path (relative to migrations)
     */
    private String archiveDirectory = "archive";

    /**
     * Repeatable migrations directory
     */
    private String repeatableDirectory = "repeatable";

    // Getters and Setters

    public boolean isBaselineEnabled() {
        return baselineEnabled;
    }

    public void setBaselineEnabled(boolean baselineEnabled) {
        this.baselineEnabled = baselineEnabled;
    }

    public boolean isValidationEnabled() {
        return validationEnabled;
    }

    public void setValidationEnabled(boolean validationEnabled) {
        this.validationEnabled = validationEnabled;
    }

    public boolean isDriftDetectionEnabled() {
        return driftDetectionEnabled;
    }

    public void setDriftDetectionEnabled(boolean driftDetectionEnabled) {
        this.driftDetectionEnabled = driftDetectionEnabled;
    }

    public boolean isFailOnValidationError() {
        return failOnValidationError;
    }

    public void setFailOnValidationError(boolean failOnValidationError) {
        this.failOnValidationError = failOnValidationError;
    }

    public boolean isFailOnDrift() {
        return failOnDrift;
    }

    public void setFailOnDrift(boolean failOnDrift) {
        this.failOnDrift = failOnDrift;
    }

    public boolean isModularEnabled() {
        return modularEnabled;
    }

    public void setModularEnabled(boolean modularEnabled) {
        this.modularEnabled = modularEnabled;
    }

    public List<String> getModules() {
        return modules;
    }

    public void setModules(List<String> modules) {
        this.modules = modules;
    }

    public boolean isAsyncExecution() {
        return asyncExecution;
    }

    public void setAsyncExecution(boolean asyncExecution) {
        this.asyncExecution = asyncExecution;
    }

    public long getLongRunningThresholdSeconds() {
        return longRunningThresholdSeconds;
    }

    public void setLongRunningThresholdSeconds(long longRunningThresholdSeconds) {
        this.longRunningThresholdSeconds = longRunningThresholdSeconds;
    }

    public boolean isBackwardCompatibilityChecks() {
        return backwardCompatibilityChecks;
    }

    public void setBackwardCompatibilityChecks(boolean backwardCompatibilityChecks) {
        this.backwardCompatibilityChecks = backwardCompatibilityChecks;
    }

    public boolean isAllowDestructiveOperations() {
        return allowDestructiveOperations;
    }

    public void setAllowDestructiveOperations(boolean allowDestructiveOperations) {
        this.allowDestructiveOperations = allowDestructiveOperations;
    }

    public int getArchiveAfterDays() {
        return archiveAfterDays;
    }

    public void setArchiveAfterDays(int archiveAfterDays) {
        this.archiveAfterDays = archiveAfterDays;
    }

    public boolean isArchivalEnabled() {
        return archivalEnabled;
    }

    public void setArchivalEnabled(boolean archivalEnabled) {
        this.archivalEnabled = archivalEnabled;
    }

    public String getBaselineFileName() {
        return baselineFileName;
    }

    public void setBaselineFileName(String baselineFileName) {
        this.baselineFileName = baselineFileName;
    }

    public String getArchiveDirectory() {
        return archiveDirectory;
    }

    public void setArchiveDirectory(String archiveDirectory) {
        this.archiveDirectory = archiveDirectory;
    }

    public String getRepeatableDirectory() {
        return repeatableDirectory;
    }

    public void setRepeatableDirectory(String repeatableDirectory) {
        this.repeatableDirectory = repeatableDirectory;
    }
}
