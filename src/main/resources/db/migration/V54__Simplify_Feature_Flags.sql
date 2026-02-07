-- Simplify feature flags to one per module
-- Remove granular feature flags and keep only module-level flags

-- Delete granular feature flags that are now consolidated into module flags
DELETE FROM feature_config WHERE feature_flag IN (
    -- Budget sub-features (now part of BUDGET_MODULE)
    'EXPENSES',
    'INCOME',
    'ALERTS',
    'RECURRING_TRANSACTIONS',
    'CUSTOM_CATEGORIES',
    'CASH_FLOW_ANALYSIS',
    'SUBSCRIPTIONS',
    
    -- Tax sub-features (now part of TAX_MODULE)
    'TAX_REGIME_COMPARISON',
    'CAPITAL_GAINS',
    'TAX_SAVING_RECOMMENDATIONS',
    'TDS_TRACKING',
    'TAX_PROJECTIONS',
    'ITR_EXPORT',
    'TAX_MANAGEMENT',
    
    -- Investment sub-features (now part of INVESTMENTS_MODULE)
    'PORTFOLIO',
    'STOCKS',
    'MUTUAL_FUNDS',
    'BONDS',
    'GOLD',
    'ETF',
    'REAL_ESTATE',
    
    -- Banking sub-features (now part of BANKING_MODULE)
    'BANK_ACCOUNTS',
    'CREDIT_CARDS',
    'LOANS',
    'FIXED_DEPOSITS',
    'RECURRING_DEPOSITS',
    
    -- Insurance sub-features (now part of INSURANCE_MODULE)
    'INSURANCE',
    'LIFE_INSURANCE',
    'HEALTH_INSURANCE',
    
    -- Net Worth sub-features (now part of NET_WORTH_MODULE)
    'NET_WORTH',
    'ASSET_ALLOCATION',
    
    -- Admin sub-features (now part of ADMIN_MODULE)
    'ADMIN_PORTAL',
    'USER_MANAGEMENT',
    'AUDIT_LOGS',
    'REPORTS',
    
    -- Future features (removed for now)
    'RECEIPT_MANAGEMENT',
    'SPLIT_EXPENSES',
    'BUDGET_FORECASTING',
    'FINANCIAL_GOALS',
    'MULTI_CURRENCY'
);

-- Update existing module flags with comprehensive descriptions
UPDATE feature_config SET 
    description = 'Budget and expense management with alerts, subscriptions, and recurring transactions',
    updated_at = CURRENT_TIMESTAMP
WHERE feature_flag = 'BUDGET_MODULE';

UPDATE feature_config SET 
    description = 'Tax planning, regime comparison, capital gains, and ITR management',
    updated_at = CURRENT_TIMESTAMP
WHERE feature_flag = 'TAX_MODULE';

-- Ensure all module-level flags exist with correct data
INSERT IGNORE INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) VALUES
('INVESTMENTS_MODULE', TRUE, 'Portfolio tracking for stocks, mutual funds, bonds, gold, ETF, and real estate', 'investments', TRUE, FALSE, FALSE),
('BANKING_MODULE', TRUE, 'Manage bank accounts, credit cards, loans, FDs, and RDs', 'banking', TRUE, FALSE, FALSE),
('INSURANCE_MODULE', TRUE, 'Track life, health, and other insurance policies', 'insurance', TRUE, FALSE, FALSE),
('NET_WORTH_MODULE', TRUE, 'Calculate net worth and track asset allocation', 'networth', TRUE, FALSE, FALSE),
('ADMIN_MODULE', TRUE, 'Administrative features including user management and audit logs', 'admin', FALSE, FALSE, FALSE);
