-- Add TAX_MANAGEMENT feature flag
INSERT INTO feature_config (feature_flag, description, enabled, category, enabled_for_all, requires_subscription, beta_feature)
VALUES ('TAX_MANAGEMENT', 'Comprehensive tax planning, filing, and management tools', TRUE, 'tax', TRUE, FALSE, FALSE)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    enabled = VALUES(enabled),
    updated_at = CURRENT_TIMESTAMP;
