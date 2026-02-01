-- V30: Create alerts table for budget overspending notifications
-- Author: System
-- Date: 2026-02-01

CREATE TABLE alerts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_type VARCHAR(20) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    message VARCHAR(500) NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    amount_spent DECIMAL(15, 2) NOT NULL,
    percentage_used DECIMAL(5, 2) NOT NULL,
    month_year VARCHAR(7) NOT NULL,
    is_read BOOLEAN DEFAULT FALSE NOT NULL,
    notification_sent BOOLEAN DEFAULT FALSE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    read_at TIMESTAMP NULL,
    
    INDEX idx_user_alerts (user_id, created_at DESC),
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_user_month (user_id, month_year),
    INDEX idx_user_category_month (user_id, category, month_year),
    
    CONSTRAINT fk_alert_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments for documentation
ALTER TABLE alerts COMMENT = 'Budget alerts and notifications for users';
