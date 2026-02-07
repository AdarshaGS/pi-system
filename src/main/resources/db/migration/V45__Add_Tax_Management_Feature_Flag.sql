-- Add TAX_MANAGEMENT feature flag
INSERT INTO feature_config (feature_key, feature_name, description, enabled, created_at)
VALUES ('TAX_MANAGEMENT', 'Tax Management', 'Comprehensive tax planning, filing, and management tools', true, CURRENT_TIMESTAMP)
ON CONFLICT (feature_key) DO UPDATE SET
    enabled = true,
    description = 'Comprehensive tax planning, filing, and management tools';
