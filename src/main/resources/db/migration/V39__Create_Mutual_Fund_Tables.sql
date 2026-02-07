-- V35__Create_Mutual_Fund_Tables.sql
-- Create tables for Mutual Fund transaction and holding management

-- Mutual Fund Master Data
CREATE TABLE IF NOT EXISTS mutual_funds (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    scheme_code VARCHAR(50) NOT NULL UNIQUE,
    scheme_name VARCHAR(500) NOT NULL,
    fund_house VARCHAR(255) NOT NULL,
    scheme_type VARCHAR(100),
    scheme_category VARCHAR(100),
    nav DECIMAL(15,4),
    nav_date DATE,
    expense_ratio DECIMAL(5,2),
    aum DECIMAL(20,2),
    min_investment DECIMAL(15,2),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_scheme_code (scheme_code),
    INDEX idx_fund_house (fund_house),
    INDEX idx_scheme_category (scheme_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mutual Fund Transactions
CREATE TABLE IF NOT EXISTS mutual_fund_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mutual_fund_id BIGINT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL COMMENT 'BUY, SELL, DIVIDEND_REINVEST, SIP',
    transaction_date DATE NOT NULL,
    units DECIMAL(15,4) NOT NULL,
    nav DECIMAL(15,4) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    stamp_duty DECIMAL(10,2) DEFAULT 0,
    transaction_charges DECIMAL(10,2) DEFAULT 0,
    stt DECIMAL(10,2) DEFAULT 0 COMMENT 'Securities Transaction Tax',
    folio_number VARCHAR(50),
    notes TEXT,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds(id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_mutual_fund_id (mutual_fund_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type),
    INDEX idx_folio_number (folio_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Mutual Fund Holdings (Consolidated View)
CREATE TABLE IF NOT EXISTS mutual_fund_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mutual_fund_id BIGINT NOT NULL,
    folio_number VARCHAR(50),
    total_units DECIMAL(15,4) NOT NULL DEFAULT 0,
    average_nav DECIMAL(15,4) NOT NULL DEFAULT 0,
    invested_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    current_nav DECIMAL(15,4),
    current_value DECIMAL(15,2),
    unrealized_gain DECIMAL(15,2),
    unrealized_gain_percentage DECIMAL(10,2),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds(id) ON DELETE RESTRICT,
    UNIQUE KEY unique_user_fund_folio (user_id, mutual_fund_id, folio_number),
    INDEX idx_user_id (user_id),
    INDEX idx_mutual_fund_id (mutual_fund_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- SIP (Systematic Investment Plan) Configuration
CREATE TABLE IF NOT EXISTS sip_configurations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    mutual_fund_id BIGINT NOT NULL,
    sip_amount DECIMAL(15,2) NOT NULL,
    frequency VARCHAR(20) NOT NULL COMMENT 'MONTHLY, QUARTERLY, WEEKLY',
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE NOT NULL,
    folio_number VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    auto_debit BOOLEAN DEFAULT FALSE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (mutual_fund_id) REFERENCES mutual_funds(id) ON DELETE RESTRICT,
    INDEX idx_user_id (user_id),
    INDEX idx_next_execution_date (next_execution_date),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert some sample mutual funds
INSERT INTO mutual_funds (scheme_code, scheme_name, fund_house, scheme_type, scheme_category, nav, nav_date, expense_ratio, min_investment) VALUES
('120503', 'HDFC Balanced Advantage Fund - Direct Plan - Growth', 'HDFC Mutual Fund', 'Hybrid', 'Balanced Advantage', 350.50, '2026-02-01', 0.85, 5000),
('120716', 'HDFC Top 100 Fund - Direct Plan - Growth', 'HDFC Mutual Fund', 'Equity', 'Large Cap', 825.75, '2026-02-01', 0.75, 5000),
('118989', 'ICICI Prudential Bluechip Fund - Direct Plan - Growth', 'ICICI Prudential Mutual Fund', 'Equity', 'Large Cap', 95.50, '2026-02-01', 0.80, 5000),
('119551', 'SBI Small Cap Fund - Direct Plan - Growth', 'SBI Mutual Fund', 'Equity', 'Small Cap', 145.30, '2026-02-01', 0.95, 5000),
('119597', 'Axis Midcap Fund - Direct Plan - Growth', 'Axis Mutual Fund', 'Equity', 'Mid Cap', 87.40, '2026-02-01', 0.85, 5000);
