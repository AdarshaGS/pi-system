-- ============================================================================
-- Migration History Table
--
-- Tracks all database migrations with extended metadata beyond Flyway's
-- schema_history table. Provides audit trail and observability.
-- ============================================================================

CREATE TABLE IF NOT EXISTS migration_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    version VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    module VARCHAR(50),
    script VARCHAR(500) NOT NULL,
    checksum VARCHAR(64) NOT NULL,
    execution_order INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    execution_time_ms BIGINT,
    executed_by VARCHAR(100),
    error_message TEXT,
    is_repeatable BOOLEAN NOT NULL DEFAULT FALSE,
    is_baseline BOOLEAN NOT NULL DEFAULT FALSE,
    validated_at TIMESTAMP,
    application_version VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_migration_version (version),
    INDEX idx_migration_module (module),
    INDEX idx_migration_status (status),
    INDEX idx_migration_executed_at (executed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Extended migration execution history with audit trail';
