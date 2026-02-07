-- Add subscription_tier column to users table
-- Default all existing users to FREE tier

ALTER TABLE users ADD COLUMN subscription_tier VARCHAR(50) DEFAULT 'FREE';

-- Update existing users to have FREE tier
UPDATE users SET subscription_tier = 'FREE' WHERE subscription_tier IS NULL;
