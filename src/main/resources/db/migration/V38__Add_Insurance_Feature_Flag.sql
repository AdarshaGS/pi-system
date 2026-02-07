-- Add Insurance feature flag to feature_config table

INSERT INTO feature_config (feature_flag, enabled, description, category, enabled_for_all, requires_subscription, beta_feature) 
VALUES ('INSURANCE', TRUE, 'Life and Health Insurance Management', 'wealth', TRUE, FALSE, FALSE);
