-- V52: Add Additional Performance Indexes for Advanced Features
-- Migration to add supplementary indexes for optimal query performance
-- Covers complex queries used in financial planning and analytics
-- Note: MySQL does not support IF NOT EXISTS, WHERE clauses, INCLUDE, or COMMENT ON INDEX

-- Financial Goals - Additional indexes for complex queries
CREATE INDEX idx_financial_goals_completion ON financial_goals(user_id, completed_at, status);
CREATE INDEX idx_financial_goals_upcoming ON financial_goals(user_id, target_date, status);
CREATE INDEX idx_financial_goals_overdue ON financial_goals(user_id, target_date, status);

-- Recurring Transactions - Additional indexes for scheduler optimization
CREATE INDEX idx_recurring_tx_due_today ON recurring_transactions(id, next_execution_date, status, auto_execute);
CREATE INDEX idx_recurring_tx_reminders ON recurring_transactions(id, next_execution_date, reminder_days_before, status, send_reminder);

-- Documents - Additional indexes for expiry tracking
CREATE INDEX idx_documents_expiring_soon ON documents(user_id, expiry_date, is_active);
CREATE INDEX idx_documents_expired ON documents(user_id, expiry_date, is_active);
CREATE INDEX idx_documents_needs_verification ON documents(user_id, uploaded_at, is_active, is_verified);

-- Cash Flow Records - Additional indexes for trending and analysis
CREATE INDEX idx_cash_flow_monthly_trend ON cash_flow_records(user_id, record_date, period_type);
CREATE INDEX idx_cash_flow_recent ON cash_flow_records(user_id, record_date, period_type);

-- Credit Scores - Additional indexes for trend analysis
CREATE INDEX idx_credit_scores_recent_changes ON credit_scores(user_id, record_date, change_from_previous);

-- Add covering indexes for common queries (MySQL doesn't support INCLUDE, so add columns directly)
CREATE INDEX idx_financial_goals_dashboard ON financial_goals(user_id, status, priority, target_date, goal_name, target_amount, current_amount, progress_percentage);
CREATE INDEX idx_recurring_tx_dashboard ON recurring_transactions(user_id, status, type, name, amount, frequency, next_execution_date);
