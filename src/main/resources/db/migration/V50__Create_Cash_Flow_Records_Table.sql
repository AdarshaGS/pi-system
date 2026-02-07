-- V50: Create Cash Flow Records Table
-- Migration to support cash flow analysis and projections
-- Enables tracking of income/expenses and future cash flow projections

CREATE TABLE IF NOT EXISTS cash_flow_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    total_income DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (total_income >= 0),
    total_expenses DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (total_expenses >= 0),
    net_cash_flow DECIMAL(15, 2) NOT NULL DEFAULT 0,
    
    -- Income breakdown
    salary_income DECIMAL(15, 2) DEFAULT 0 CHECK (salary_income >= 0),
    investment_income DECIMAL(15, 2) DEFAULT 0 CHECK (investment_income >= 0),
    business_income DECIMAL(15, 2) DEFAULT 0 CHECK (business_income >= 0),
    other_income DECIMAL(15, 2) DEFAULT 0 CHECK (other_income >= 0),
    
    -- Expense breakdown
    housing_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (housing_expenses >= 0),
    transportation_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (transportation_expenses >= 0),
    food_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (food_expenses >= 0),
    utilities_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (utilities_expenses >= 0),
    entertainment_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (entertainment_expenses >= 0),
    healthcare_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (healthcare_expenses >= 0),
    education_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (education_expenses >= 0),
    debt_payments DECIMAL(15, 2) DEFAULT 0 CHECK (debt_payments >= 0),
    savings_contributions DECIMAL(15, 2) DEFAULT 0 CHECK (savings_contributions >= 0),
    investment_contributions DECIMAL(15, 2) DEFAULT 0 CHECK (investment_contributions >= 0),
    other_expenses DECIMAL(15, 2) DEFAULT 0 CHECK (other_expenses >= 0),
    
    period_type VARCHAR(20) NOT NULL CHECK (period_type IN ('DAILY', 'WEEKLY', 'MONTHLY', 'QUARTERLY', 'ANNUAL')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_cash_flow_record_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_cash_flow_user_date_period UNIQUE (user_id, record_date, period_type)
);

-- Create indexes for performance optimization
CREATE INDEX idx_cash_flow_user_id ON cash_flow_records(user_id);
CREATE INDEX idx_cash_flow_record_date ON cash_flow_records(record_date);
CREATE INDEX idx_cash_flow_user_date ON cash_flow_records(user_id, record_date);
CREATE INDEX idx_cash_flow_period_type ON cash_flow_records(period_type);
CREATE INDEX idx_cash_flow_user_period ON cash_flow_records(user_id, period_type);

-- Note: Net cash flow should be calculated in application code
-- MySQL triggers have different syntax than PostgreSQL
