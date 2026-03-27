-- Migration: Enhance SMS Transactions Table for Advanced Parsing Engine
-- Date: 2026-03-24
-- Description: Add new columns and update check constraints for enhanced SMS parsing with categories, tags, recurring detection, and self-transfer detection

-- Add new columns for enhanced parsing
ALTER TABLE sms_transactions 
    ADD COLUMN category VARCHAR(100) NULL COMMENT 'Dynamic string-based category (food, rent, insurance, etc.)';

ALTER TABLE sms_transactions 
    ADD COLUMN tags VARCHAR(255) NULL COMMENT 'Comma-separated tags: UPI, ATM, CARD, RECURRING';

ALTER TABLE sms_transactions 
    ADD COLUMN is_recurring BOOLEAN DEFAULT FALSE COMMENT 'Indicates if transaction is recurring (policy, premium, subscription, EMI)';

ALTER TABLE sms_transactions 
    ADD COLUMN date_from_message BOOLEAN NULL COMMENT 'True if date extracted from message, false if from SMS timestamp';

ALTER TABLE sms_transactions 
    ADD COLUMN from_account VARCHAR(50) NULL COMMENT 'Source account number for transfers';

ALTER TABLE sms_transactions 
    ADD COLUMN to_account VARCHAR(50) NULL COMMENT 'Destination account number for transfers';

-- Drop the old check constraints before modifying them
-- Note: MySQL doesn't support named constraints in some versions, so we need to drop and recreate

-- First, let's get the current constraint names and drop them
ALTER TABLE sms_transactions 
    DROP CHECK sms_transactions_chk_1;

ALTER TABLE sms_transactions 
    DROP CHECK sms_transactions_chk_2;

-- Recreate the check constraints with new values
ALTER TABLE sms_transactions 
    ADD CONSTRAINT chk_transaction_type 
    CHECK (transaction_type IN ('DEBIT', 'CREDIT', 'UNKNOWN', 'COMPLEX_TRANSACTION'));

ALTER TABLE sms_transactions 
    ADD CONSTRAINT chk_parse_status 
    CHECK (parse_status IN ('SUCCESS', 'PARTIAL', 'FAILED', 'LOW_CONFIDENCE'));

-- Add indexes for the new columns
CREATE INDEX idx_sms_category ON sms_transactions(category);
CREATE INDEX idx_sms_is_recurring ON sms_transactions(is_recurring);
CREATE INDEX idx_sms_tags ON sms_transactions(tags);
CREATE INDEX idx_sms_from_account ON sms_transactions(from_account);
CREATE INDEX idx_sms_to_account ON sms_transactions(to_account);

-- Add comment to document changes
ALTER TABLE sms_transactions 
    COMMENT = 'Stores parsed SMS transaction data with enhanced fields for categories, tags, recurring detection, and self-transfer detection';
