CREATE TABLE alert_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    symbol VARCHAR(255),
    target_price DECIMAL(19,2),
    price_condition VARCHAR(20),
    days_before_due INT,
    percentage_change DECIMAL(5,2),
    channel VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME,
    last_triggered_at DATETIME,
    description VARCHAR(1024)
);

ALTER TABLE `etfs`
ADD COLUMN `description` varchar(255) NOT NULL AFTER `name`,
ADD COLUMN `entity_type` bigint NOT NULL;