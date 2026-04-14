package com.pisystem.infrastructure.migration.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a database migration execution record.
 * Extends Flyway's schema_history with additional metadata.
 */
@Entity
@Table(name = "migration_history", indexes = {
    @Index(name = "idx_migration_version", columnList = "version"),
    @Index(name = "idx_migration_module", columnList = "module"),
    @Index(name = "idx_migration_status", columnList = "status"),
    @Index(name = "idx_migration_executed_at", columnList = "executedAt")
})
public class MigrationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String version;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MigrationType type;

    @Column(length = 50)
    private String module; // e.g., "user", "loan", "payment"

    @Column(nullable = false, length = 500)
    private String script;

    @Column(nullable = false, length = 64)
    private String checksum; // SHA-256 hash of script content

    @Column(nullable = false)
    private Integer executionOrder;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MigrationStatus status;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column
    private Long executionTimeMs;

    @Column(length = 100)
    private String executedBy;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Boolean isRepeatable = false;

    @Column(nullable = false)
    private Boolean isBaseline = false;

    @Column
    private LocalDateTime validatedAt;

    @Column(length = 50)
    private String applicationVersion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (executedAt == null) {
            executedAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public MigrationType getType() { return type; }
    public void setType(MigrationType type) { this.type = type; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getScript() { return script; }
    public void setScript(String script) { this.script = script; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public Integer getExecutionOrder() { return executionOrder; }
    public void setExecutionOrder(Integer executionOrder) { this.executionOrder = executionOrder; }

    public MigrationStatus getStatus() { return status; }
    public void setStatus(MigrationStatus status) { this.status = status; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Boolean getIsRepeatable() { return isRepeatable; }
    public void setIsRepeatable(Boolean isRepeatable) { this.isRepeatable = isRepeatable; }

    public Boolean getIsBaseline() { return isBaseline; }
    public void setIsBaseline(Boolean isBaseline) { this.isBaseline = isBaseline; }

    public LocalDateTime getValidatedAt() { return validatedAt; }
    public void setValidatedAt(LocalDateTime validatedAt) { this.validatedAt = validatedAt; }

    public String getApplicationVersion() { return applicationVersion; }
    public void setApplicationVersion(String applicationVersion) { this.applicationVersion = applicationVersion; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MigrationHistory)) return false;
        MigrationHistory that = (MigrationHistory) o;
        return Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version);
    }

    @Override
    public String toString() {
        return "MigrationHistory{" +
                "version='" + version + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", executedAt=" + executedAt +
                '}';
    }
}
