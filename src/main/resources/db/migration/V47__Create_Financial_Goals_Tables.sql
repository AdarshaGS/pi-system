-- V47: Create Financial Goals and Milestones Tables
-- Migration to support financial goal tracking and planning features
-- Enables users to set, track, and achieve financial goals with milestone tracking

CREATE TABLE IF NOT EXISTS financial_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    goal_type VARCHAR(50) NOT NULL CHECK (goal_type IN (
        'RETIREMENT', 'HOME_PURCHASE', 'EDUCATION', 'EMERGENCY_FUND', 'VACATION',
        'CAR_PURCHASE', 'DEBT_PAYOFF', 'WEDDING', 'BUSINESS_START', 'INVESTMENT', 'OTHER'
    )),
    target_amount DECIMAL(15, 2) NOT NULL CHECK (target_amount > 0),
    current_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (current_amount >= 0),
    target_date DATE NOT NULL,
    start_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'PAUSED', 'CANCELLED', 'OVERDUE')),
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    expected_return_rate DECIMAL(5, 2),
    monthly_contribution DECIMAL(15, 2),
    linked_accounts VARCHAR(500),
    auto_contribute BOOLEAN DEFAULT FALSE,
    reminder_day_of_month INTEGER CHECK (reminder_day_of_month BETWEEN 1 AND 31),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    progress_percentage DECIMAL(5, 2) DEFAULT 0 CHECK (progress_percentage >= 0 AND progress_percentage <= 100),
    notes VARCHAR(500),
    
    CONSTRAINT fk_financial_goal_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS goal_milestones (
    iid BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    milestone_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    target_amount DECIMAL(15, 2) NOT NULL CHECK (target_amount > 0),
    target_date DATE NOT NULL,
    achieved_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACHIEVED', 'MISSED')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_milestone_goal FOREIGN KEY (goal_id) REFERENCES financial_goals(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
CREATE INDEX idx_financial_goals_user_id ON financial_goals(user_id);
CREATE INDEX idx_financial_goals_status ON financial_goals(status);
CREATE INDEX idx_financial_goals_goal_type ON financial_goals(goal_type);
CREATE INDEX idx_financial_goals_target_date ON financial_goals(target_date);
CREATE INDEX idx_financial_goals_user_status ON financial_goals(user_id, status);
CREATE INDEX idx_financial_goals_priority ON financial_goals(priority);
CREATE INDEX idx_financial_goals_auto_contribute ON financial_goals(auto_contribute, reminder_day_of_month);

CREATE INDEX idx_goal_milestones_goal_id ON goal_milestones(goal_id);
CREATE INDEX idx_goal_milestones_status ON goal_milestones(status);
CREATE INDEX idx_goal_milestones_target_date ON goal_milestones(target_date);