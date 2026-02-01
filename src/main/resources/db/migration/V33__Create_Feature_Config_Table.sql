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

-- Insert default feature configurations
-- Budget Module Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('BUDGET_MODULE', TRUE, 'Complete budget management with expense tracking', 'budget', TRUE, FALSE, FALSE),
('EXPENSES', TRUE, 'Track and categorize expenses', 'budget', TRUE, FALSE, FALSE),
('INCOME', TRUE, 'Track income sources', 'budget', TRUE, FALSE, FALSE),
('ALERTS', TRUE, 'Budget limit alerts and notifications', 'budget', TRUE, FALSE, FALSE),
('RECURRING_TRANSACTIONS', TRUE, 'Automated recurring transactions', 'budget', TRUE, FALSE, FALSE),
('CUSTOM_CATEGORIES', TRUE, 'User-defined expense categories', 'budget', TRUE, FALSE, FALSE),
('CASH_FLOW_ANALYSIS', TRUE, 'Cash flow analysis and reporting', 'budget', TRUE, FALSE, FALSE),
('SUBSCRIPTIONS', TRUE, 'Track recurring subscriptions', 'budget', TRUE, FALSE, FALSE);

-- Tax Module Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('TAX_MODULE', TRUE, 'Complete tax management and planning', 'tax', TRUE, FALSE, FALSE),
('TAX_REGIME_COMPARISON', TRUE, 'Compare Old vs New tax regime', 'tax', TRUE, FALSE, FALSE),
('CAPITAL_GAINS', TRUE, 'Track and calculate capital gains', 'tax', TRUE, FALSE, FALSE),
('TAX_SAVING_RECOMMENDATIONS', TRUE, 'AI-powered tax saving suggestions', 'tax', TRUE, FALSE, FALSE),
('TDS_TRACKING', TRUE, 'Track TDS entries and reconciliation', 'tax', TRUE, FALSE, FALSE),
('TAX_PROJECTIONS', TRUE, 'Project tax liability for current FY', 'tax', TRUE, FALSE, FALSE),
('ITR_EXPORT', TRUE, 'Export ITR pre-fill data', 'tax', TRUE, FALSE, FALSE);

-- Investment Module Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('PORTFOLIO', TRUE, 'Track investment portfolio', 'investments', TRUE, FALSE, FALSE),
('STOCKS', TRUE, 'Track stock investments', 'investments', TRUE, FALSE, FALSE),
('MUTUAL_FUNDS', TRUE, 'Track mutual fund investments', 'investments', TRUE, FALSE, FALSE),
('BONDS', TRUE, 'Track bond investments', 'investments', TRUE, FALSE, FALSE),
('GOLD', TRUE, 'Track gold investments', 'investments', TRUE, FALSE, FALSE),
('ETF', TRUE, 'Track ETF investments', 'investments', TRUE, FALSE, FALSE),
('REAL_ESTATE', TRUE, 'Track real estate investments', 'investments', TRUE, FALSE, FALSE);

-- Banking Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('BANK_ACCOUNTS', TRUE, 'Manage bank accounts', 'banking', TRUE, FALSE, FALSE),
('CREDIT_CARDS', TRUE, 'Track credit cards', 'banking', TRUE, FALSE, FALSE),
('LOANS', TRUE, 'Track loans and EMIs', 'banking', TRUE, FALSE, FALSE),
('FIXED_DEPOSITS', TRUE, 'Track FD investments', 'banking', TRUE, FALSE, FALSE),
('RECURRING_DEPOSITS', TRUE, 'Track RD investments', 'banking', TRUE, FALSE, FALSE);

-- Insurance Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('INSURANCE', TRUE, 'Track insurance policies', 'insurance', TRUE, FALSE, FALSE),
('LIFE_INSURANCE', TRUE, 'Track life insurance policies', 'insurance', TRUE, FALSE, FALSE),
('HEALTH_INSURANCE', TRUE, 'Track health insurance policies', 'insurance', TRUE, FALSE, FALSE);

-- Net Worth Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('NET_WORTH', TRUE, 'Calculate and track net worth', 'networth', TRUE, FALSE, FALSE),
('ASSET_ALLOCATION', TRUE, 'Track asset allocation', 'networth', TRUE, FALSE, FALSE);

-- Admin Features
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('ADMIN_PORTAL', TRUE, 'Administrative features', 'admin', FALSE, FALSE, FALSE),
('USER_MANAGEMENT', TRUE, 'Manage users', 'admin', FALSE, FALSE, FALSE),
('AUDIT_LOGS', TRUE, 'View audit logs', 'admin', FALSE, FALSE, FALSE),
('REPORTS', TRUE, 'Generate reports', 'admin', TRUE, FALSE, FALSE);

-- Future Features (Disabled by default)
INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('RECEIPT_MANAGEMENT', FALSE, 'Upload and manage receipts', 'budget', TRUE, FALSE, TRUE),
('SPLIT_EXPENSES', FALSE, 'Split expenses with others', 'budget', TRUE, FALSE, TRUE),
('BUDGET_FORECASTING', FALSE, 'AI-powered budget predictions', 'budget', TRUE, TRUE, TRUE),
('FINANCIAL_GOALS', FALSE, 'Track financial goals', 'planning', TRUE, FALSE, TRUE),
('MULTI_CURRENCY', FALSE, 'Support multiple currencies', 'core', TRUE, TRUE, TRUE);

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
