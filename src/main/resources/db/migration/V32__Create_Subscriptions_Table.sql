-- Migration for Subscription Management feature
-- Creates subscriptions table for tracking recurring subscriptions (Netflix, Spotify, etc.)

CREATE TABLE subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    service_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(20) NOT NULL,
    category VARCHAR(30) NOT NULL,
    start_date DATE NOT NULL,
    next_renewal_date DATE,
    cancellation_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    auto_renewal BOOLEAN NOT NULL DEFAULT TRUE,
    payment_method VARCHAR(50),
    reminder_days_before INT NOT NULL DEFAULT 3,
    last_used_date DATE,
    notes VARCHAR(1000),
    website_url VARCHAR(300),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_user_category (user_id, category),
    INDEX idx_next_renewal (next_renewal_date),
    INDEX idx_status (status),
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_billing_cycle CHECK (billing_cycle IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'HALF_YEARLY', 'YEARLY')),
    CONSTRAINT chk_category CHECK (category IN ('ENTERTAINMENT', 'SOFTWARE', 'CLOUD_STORAGE', 'NEWS_MEDIA', 'FITNESS', 'EDUCATION', 'GAMING', 'UTILITIES', 'FOOD_DELIVERY', 'SHOPPING', 'OTHER')),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED', 'PAUSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Comments for documentation
ALTER TABLE subscriptions 
    COMMENT = 'Tracks recurring subscriptions like Netflix, Spotify, etc. with renewal reminders and cost analysis';

ALTER TABLE subscriptions 
    MODIFY COLUMN service_name VARCHAR(200) NOT NULL COMMENT 'Name of the service (e.g., Netflix, Spotify)',
    MODIFY COLUMN amount DECIMAL(10, 2) NOT NULL COMMENT 'Subscription amount per billing cycle',
    MODIFY COLUMN billing_cycle VARCHAR(20) NOT NULL COMMENT 'Frequency: WEEKLY, MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY',
    MODIFY COLUMN category VARCHAR(30) NOT NULL COMMENT 'Category: ENTERTAINMENT, SOFTWARE, etc.',
    MODIFY COLUMN next_renewal_date DATE COMMENT 'Next billing/renewal date',
    MODIFY COLUMN auto_renewal BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Whether subscription auto-renews',
    MODIFY COLUMN reminder_days_before INT NOT NULL DEFAULT 3 COMMENT 'Days before renewal to send reminder',
    MODIFY COLUMN last_used_date DATE COMMENT 'Last date the service was used (for unused detection)';
