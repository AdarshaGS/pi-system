-- V36__Create_ETF_Tables.sql
-- Create tables for ETF (Exchange-Traded Fund) management

-- ETF Master Data
CREATE TABLE IF NOT EXISTS etfs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    isin VARCHAR(20) UNIQUE,
    exchange VARCHAR(10) NOT NULL COMMENT 'NSE, BSE',
    etf_type VARCHAR(50) NOT NULL COMMENT 'INDEX, GOLD, SILVER, INTERNATIONAL, SECTORAL',
    underlying_index VARCHAR(100),
    fund_house VARCHAR(255) NOT NULL,
    expense_ratio DECIMAL(5,2),
    aum DECIMAL(20,2) COMMENT 'Assets Under Management',
    nav DECIMAL(15,4),
    market_price DECIMAL(15,4),
    price_date DATE,
    tracking_error DECIMAL(5,2),
    dividend_yield DECIMAL(5,2),
    lot_size INT DEFAULT 1,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_symbol (symbol),
    INDEX idx_etf_type (etf_type),
    INDEX idx_exchange (exchange)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ETF Transactions
CREATE TABLE IF NOT EXISTS etf_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    etf_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL COMMENT 'BUY, SELL',
    transaction_date DATE NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(15,4) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    brokerage DECIMAL(10,2) DEFAULT 0,
    stt DECIMAL(10,2) DEFAULT 0 COMMENT 'Securities Transaction Tax',
    stamp_duty DECIMAL(10,2) DEFAULT 0,
    transaction_charges DECIMAL(10,2) DEFAULT 0,
    gst DECIMAL(10,2) DEFAULT 0,
    total_charges DECIMAL(10,2) DEFAULT 0,
    net_amount DECIMAL(15,2) NOT NULL,
    exchange VARCHAR(10) NOT NULL,
    order_id VARCHAR(50),
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (etf_id) REFERENCES etfs(id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_etf_id (etf_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ETF Holdings (Consolidated View)
CREATE TABLE IF NOT EXISTS etf_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    etf_id BIGINT NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0,
    average_price DECIMAL(15,4) NOT NULL DEFAULT 0,
    invested_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    current_price DECIMAL(15,4),
    current_value DECIMAL(15,2),
    unrealized_gain DECIMAL(15,2),
    unrealized_gain_percentage DECIMAL(10,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (etf_id) REFERENCES etfs(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_user_etf (user_id, etf_id),
    INDEX idx_user_id (user_id),
    INDEX idx_etf_id (etf_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ETF Price History
CREATE TABLE IF NOT EXISTS etf_price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    etf_id BIGINT NOT NULL,
    price_date DATE NOT NULL,
    open_price DECIMAL(15,4),
    high_price DECIMAL(15,4),
    low_price DECIMAL(15,4),
    close_price DECIMAL(15,4),
    nav DECIMAL(15,4),
    volume BIGINT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (etf_id) REFERENCES etfs(id) ON DELETE CASCADE,
    UNIQUE KEY unique_etf_date (etf_id, price_date),
    INDEX idx_etf_id (etf_id),
    INDEX idx_price_date (price_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample ETFs
INSERT IGNORE INTO etfs (symbol, name, isin, exchange, etf_type, underlying_index, fund_house, expense_ratio, nav, market_price, price_date, lot_size) VALUES
('NIFTYBEES', 'Nippon India ETF Nifty BeES', 'INF204KB14I2', 'NSE', 'INDEX', 'NIFTY 50', 'Nippon India Mutual Fund', 0.05, 240.50, 240.75, '2026-02-01', 1),
('GOLDBEES', 'Nippon India ETF Gold BeES', 'INF204KB13I4', 'NSE', 'GOLD', 'Gold Price', 'Nippon India Mutual Fund', 0.50, 62.30, 62.35, '2026-02-01', 1),
('LIQUIDBEES', 'Nippon India ETF Liquid BeES', 'INF204KB11I8', 'NSE', 'LIQUID', 'Liquid Fund', 'Nippon India Mutual Fund', 0.15, 1000.10, 1000.12, '2026-02-01', 1),
('BANKBEES', 'Nippon India ETF Bank BeES', 'INF204KB12I6', 'NSE', 'SECTORAL', 'NIFTY Bank', 'Nippon India Mutual Fund', 0.25, 485.20, 485.50, '2026-02-01', 1),
('JUNIORBEES', 'Nippon India ETF Junior BeES', 'INF204KB15I9', 'NSE', 'INDEX', 'NIFTY Next 50', 'Nippon India Mutual Fund', 0.30, 625.40, 625.75, '2026-02-01', 1);
