-- ============================================================================
-- V60: Sub-feature flags — hierarchical parent-child feature config
--
-- Adds:
--   1. parent_feature_flag column to feature_config (nullable self-reference)
--   2. Sub-feature rows for Budget, Tax, Investments, Banking, Insurance modules
--
-- Rule: a sub-feature is only active when BOTH itself AND its parent module are
--       enabled. The application enforces this at query time (FeatureConfigService).
-- ============================================================================

-- 1. Add parent column (nullable — module-level flags have no parent)
ALTER TABLE feature_config
    ADD COLUMN IF NOT EXISTS parent_feature_flag VARCHAR(100) NULL
        REFERENCES feature_config(feature_flag) DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX IF NOT EXISTS idx_fc_parent_flag ON feature_config(parent_feature_flag);

-- ============================================================================
-- 2. Budget sub-features
-- ============================================================================
INSERT IGNORE INTO feature_config (feature_flag, parent_feature_flag, enabled, enabled_for_all, description, category, requires_subscription, beta_feature)
VALUES
    ('BUDGET_EXPENSES',              'BUDGET_MODULE', TRUE, TRUE, 'Track and categorise spending transactions',        'budget', FALSE, FALSE),
    ('BUDGET_INCOME',                'BUDGET_MODULE', TRUE, TRUE, 'Record and analyse income sources',                 'budget', FALSE, FALSE),
    ('BUDGET_CATEGORIES',            'BUDGET_MODULE', TRUE, TRUE, 'Custom expense and income categories',              'budget', FALSE, FALSE),
    ('BUDGET_ALERTS',                'BUDGET_MODULE', TRUE, TRUE, 'Set spending threshold alerts',                     'budget', FALSE, FALSE),
    ('BUDGET_SUBSCRIPTIONS',         'BUDGET_MODULE', TRUE, TRUE, 'Track recurring subscription payments',             'budget', FALSE, FALSE),
    ('BUDGET_RECURRING_TRANSACTIONS','BUDGET_MODULE', TRUE, TRUE, 'Automate recurring income/expense templates',       'budget', FALSE, FALSE)
ON CONFLICT (feature_flag) DO NOTHING;

-- ============================================================================
-- 3. Tax sub-features
-- ============================================================================
INSERT IGNORE INTO feature_config (feature_flag, parent_feature_flag, enabled, enabled_for_all, description, category, requires_subscription, beta_feature)
VALUES
    ('TAX_CAPITAL_GAINS',    'TAX_MODULE', TRUE, TRUE, 'Calculate short-term and long-term capital gains', 'tax', FALSE, FALSE),
    ('TAX_REGIME_COMPARISON','TAX_MODULE', TRUE, TRUE, 'Compare Old vs New income tax regimes',            'tax', FALSE, FALSE),
    ('TAX_TDS_TRACKING',     'TAX_MODULE', TRUE, TRUE, 'Track and reconcile TDS deductions',               'tax', FALSE, FALSE),
    ('TAX_ITR_ASSISTANT',    'TAX_MODULE', TRUE, TRUE, 'Step-by-step ITR filing guidance',                 'tax', FALSE, FALSE)
ON CONFLICT (feature_flag) DO NOTHING;

-- ============================================================================
-- 4. Investments sub-features
-- ============================================================================
INSERT IGNORE INTO feature_config (feature_flag, parent_feature_flag, enabled, enabled_for_all, description, category, requires_subscription, beta_feature)
VALUES
    ('INVESTMENTS_STOCKS',       'INVESTMENTS_MODULE', TRUE, TRUE, 'Track equity holdings and P&L',              'investments', FALSE, FALSE),
    ('INVESTMENTS_MUTUAL_FUNDS', 'INVESTMENTS_MODULE', TRUE, TRUE, 'Track mutual fund folios and NAV',           'investments', FALSE, FALSE),
    ('INVESTMENTS_ETF',          'INVESTMENTS_MODULE', TRUE, TRUE, 'Track ETF holdings',                         'investments', FALSE, FALSE),
    ('INVESTMENTS_BONDS',        'INVESTMENTS_MODULE', TRUE, TRUE, 'Track bonds, sovereign gold bonds, and gold','investments', FALSE, FALSE)
ON CONFLICT (feature_flag) DO NOTHING;

-- ============================================================================
-- 5. Banking sub-features
-- ============================================================================
INSERT IGNORE INTO feature_config (feature_flag, parent_feature_flag, enabled, enabled_for_all, description, category, requires_subscription, beta_feature)
VALUES
    ('BANKING_ACCOUNTS',       'BANKING_MODULE', TRUE, TRUE, 'Manage savings and current accounts',          'banking', FALSE, FALSE),
    ('BANKING_LOANS',          'BANKING_MODULE', TRUE, TRUE, 'EMI tracking and amortisation',                 'banking', FALSE, FALSE),
    ('BANKING_FIXED_DEPOSITS', 'BANKING_MODULE', TRUE, TRUE, 'Track FDs and RDs with maturity alerts',        'banking', FALSE, FALSE)
ON CONFLICT (feature_flag) DO NOTHING;

-- ============================================================================
-- 6. Insurance sub-features
-- ============================================================================
INSERT IGNORE INTO feature_config (feature_flag, parent_feature_flag, enabled, enabled_for_all, description, category, requires_subscription, beta_feature)
VALUES
    ('INSURANCE_LIFE',   'INSURANCE_MODULE', TRUE, TRUE, 'Track life cover policies and premiums', 'insurance', FALSE, FALSE),
    ('INSURANCE_HEALTH', 'INSURANCE_MODULE', TRUE, TRUE, 'Track health policies and claim history', 'insurance', FALSE, FALSE)
ON CONFLICT (feature_flag) DO NOTHING;

-- ============================================================================
-- 7. Backfill parent_feature_flag = NULL explicitly for existing module rows
--    (already NULL by default, this is a documentation no-op but makes it clear)
-- ============================================================================
UPDATE feature_config SET parent_feature_flag = NULL
WHERE feature_flag IN (
    'BUDGET_MODULE', 'TAX_MODULE', 'INVESTMENTS_MODULE',
    'BANKING_MODULE', 'INSURANCE_MODULE', 'NET_WORTH_MODULE', 'ADMIN_MODULE'
);
