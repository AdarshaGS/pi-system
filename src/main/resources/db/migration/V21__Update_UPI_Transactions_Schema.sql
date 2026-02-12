-- Add missing columns to transactions table
ALTER TABLE transactions 
ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100) UNIQUE,
ADD COLUMN IF NOT EXISTS type VARCHAR(20),
ADD COLUMN IF NOT EXISTS category VARCHAR(50),
ADD COLUMN IF NOT EXISTS merchant_name VARCHAR(255),
ADD COLUMN IF NOT EXISTS receipt_url VARCHAR(500),
ADD COLUMN IF NOT EXISTS error_code VARCHAR(50),
ADD COLUMN IF NOT EXISTS error_message TEXT,
ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP;

-- Create index on transaction_id for faster lookups
CREATE INDEX IF NOT EXISTS idx_transaction_id ON transactions(transaction_id);

-- Create index on status for faster filtering
CREATE INDEX IF NOT EXISTS idx_transaction_status ON transactions(status);

-- Add responded_at column to transaction_requests if it doesn't exist
ALTER TABLE transaction_requests 
ADD COLUMN IF NOT EXISTS responded_at TIMESTAMP;
