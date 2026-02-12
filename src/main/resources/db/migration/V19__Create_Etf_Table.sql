CREATE TABLE IF NOT EXISTS etf (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64),
    expense_ratio DECIMAL(5,4),
    aum BIGINT, -- Assets Under Management
    inception_date DATE,
    fund_house VARCHAR(128),
    benchmark VARCHAR(128),
    tracking_error DECIMAL(6,4),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description VARCHAR(1024)
);

CREATE UNIQUE INDEX idx_etf_symbol ON etf(symbol);
