-- Migration: Create SMS Transactions Table
-- Date: 2026-03-12
-- Description: Table for storing parsed SMS transaction data from various banks and payment systems

CREATE TABLE IF NOT EXISTS sms_transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    original_message TEXT NOT NULL,
    sender VARCHAR(50),
    amount DECIMAL(15,2),
    transaction_date DATE,
    transaction_time TIME,
    transaction_type VARCHAR(20) CHECK (transaction_type IN ('DEBIT', 'CREDIT', 'UNKNOWN')),
    merchant VARCHAR(255),
    account_number VARCHAR(50),
    card_number VARCHAR(50),
    balance DECIMAL(15,2),
    reference_number VARCHAR(100),
    upi_id VARCHAR(100),
    parse_status VARCHAR(20) NOT NULL CHECK (parse_status IN ('SUCCESS', 'PARTIAL', 'FAILED')),
    parse_confidence DOUBLE CHECK (parse_confidence BETWEEN 0.0 AND 1.0),
    error_message VARCHAR(500),
    is_processed BOOLEAN DEFAULT FALSE,
    linked_expense_id BIGINT,
    linked_income_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes for performance
    INDEX idx_user_id (user_id),
    INDEX idx_parse_status (parse_status),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_is_processed (is_processed),
    INDEX idx_user_status (user_id, parse_status),
    INDEX idx_user_processed (user_id, is_processed),
    INDEX idx_user_date (user_id, transaction_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments for documentation
ALTER TABLE sms_transactions 
    COMMENT = 'Stores parsed SMS transaction data from banks and payment systems';
