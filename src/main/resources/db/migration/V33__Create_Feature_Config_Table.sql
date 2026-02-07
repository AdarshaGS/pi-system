-- Migration for Feature Toggle System
-- Creates feature_config table for managing feature flags

CREATE TABLE feature_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_flag VARCHAR(100) NOT NULL UNIQUE,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    enabled_for_all BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    category VARCHAR(50),
    requires_subscription BOOLEAN NOT NULL DEFAULT FALSE,
    min_subscription_tier VARCHAR(50),
    beta_feature BOOLEAN NOT NULL DEFAULT FALSE,
    enabled_since TIMESTAMP NULL,
    disabled_since TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_feature_flag (feature_flag),
    INDEX idx_enabled (enabled),
    INDEX idx_category (category),
    CONSTRAINT chk_min_subscription_tier CHECK (min_subscription_tier IN ('FREE', 'BASIC', 'PREMIUM', 'ENTERPRISE') OR min_subscription_tier IS NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert simplified module-level feature configurations
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('BUDGET_MODULE', TRUE, 'Budget and expense management with alerts, subscriptions, and recurring transactions', 'budget', TRUE, FALSE, FALSE),
('TAX_MODULE', TRUE, 'Tax planning, regime comparison, capital gains, and ITR management', 'tax', TRUE, FALSE, FALSE),
('INVESTMENTS_MODULE', TRUE, 'Portfolio tracking for stocks, mutual funds, bonds, gold, ETF, and real estate', 'investments', TRUE, FALSE, FALSE),
('BANKING_MODULE', TRUE, 'Manage bank accounts, credit cards, loans, FDs, and RDs', 'banking', TRUE, FALSE, FALSE),
('INSURANCE_MODULE', TRUE, 'Track life, health, and other insurance policies', 'insurance', TRUE, FALSE, FALSE),
('NET_WORTH_MODULE', TRUE, 'Calculate net worth and track asset allocation', 'networth', TRUE, FALSE, FALSE),
('ADMIN_MODULE', TRUE, 'Administrative features including user management and audit logs', 'admin', FALSE, FALSE, FALSE);

-- Comments for documentation
ALTER TABLE feature_config 
    COMMENT = 'Stores feature toggle configuration for dynamic feature management';

ALTER TABLE feature_config 
    MODIFY COLUMN feature_flag VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique feature flag identifier',
    MODIFY COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether feature is currently enabled',
    MODIFY COLUMN enabled_for_all BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether feature is enabled for all users or specific users',
    MODIFY COLUMN requires_subscription BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether feature requires paid subscription',
    MODIFY COLUMN min_subscription_tier VARCHAR(50) COMMENT 'Minimum subscription tier required (FREE, BASIC, PREMIUM, ENTERPRISE)',
    MODIFY COLUMN beta_feature BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Whether this is a beta/experimental feature';
