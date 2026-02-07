-- V48: Create Recurring Transactions Tables
-- Migration to support automated recurring transaction processing
-- Enables automatic execution of scheduled transactions (bills, subscriptions, savings, etc.)

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(20) NOT NULL CHECK (type IN (
        'INCOME', 'EXPENSE', 'TRANSFER', 'INVESTMENT', 'BILL_PAYMENT',
        'LOAN_PAYMENT', 'SAVINGS', 'SUBSCRIPTION'
    )),
    amount DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    frequency VARCHAR(20) NOT NULL CHECK (frequency IN (
        'DAILY', 'WEEKLY', 'BIWEEKLY', 'MONTHLY', 'QUARTERLY', 'SEMI_ANNUAL', 'ANNUAL'
    )),
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    last_execution_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    category VARCHAR(100),
    source_account VARCHAR(100),
    destination_account VARCHAR(100),
    day_of_month INTEGER CHECK (day_of_month BETWEEN 1 AND 31),
    day_of_week INTEGER CHECK (day_of_week BETWEEN 1 AND 7),
    auto_execute BOOLEAN DEFAULT TRUE,
    send_reminder BOOLEAN DEFAULT FALSE,
    reminder_days_before INTEGER DEFAULT 1,
    execution_count INTEGER NOT NULL DEFAULT 0,
    max_executions INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes VARCHAR(500),
    
    CONSTRAINT fk_recurring_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_end_date_after_start CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE TABLE IF NOT EXISTS recurring_transaction_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recurring_transaction_id BIGINT NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('SUCCESS', 'FAILED', 'SKIPPED', 'PENDING')),
    error_message VARCHAR(500),
    transaction_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_recurring_tx_history_recurring FOREIGN KEY (recurring_transaction_id) 
        REFERENCES recurring_transactions(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
CREATE INDEX idx_recurring_tx_user_id ON recurring_transactions(user_id);
CREATE INDEX idx_recurring_tx_status ON recurring_transactions(status);
CREATE INDEX idx_recurring_tx_type ON recurring_transactions(type);
CREATE INDEX idx_recurring_tx_next_execution ON recurring_transactions(next_execution_date);
CREATE INDEX idx_recurring_tx_user_status ON recurring_transactions(user_id, status);
CREATE INDEX idx_recurring_tx_auto_execute ON recurring_transactions(auto_execute, next_execution_date, status);
CREATE INDEX idx_recurring_tx_category ON recurring_transactions(category);

CREATE INDEX idx_recurring_tx_history_recurring_id ON recurring_transaction_history(recurring_transaction_id);
CREATE INDEX idx_recurring_tx_history_executed_at ON recurring_transaction_history(executed_at);
CREATE INDEX idx_recurring_tx_history_status ON recurring_transaction_history(status);