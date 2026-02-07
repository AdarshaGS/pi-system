-- V53: Create Views for Advanced Features Analytics (MySQL)
-- Migration to create views for complex analytics
-- Note: MySQL doesn't support materialized views, so using regular views

-- View: Active Financial Goals Summary
CREATE OR REPLACE VIEW v_active_financial_goals_summary AS
SELECT 
    fg.user_id,
    COUNT(*) as total_active_goals,
    SUM(fg.target_amount) as total_target_amount,
    SUM(fg.current_amount) as total_current_amount,
    SUM(fg.target_amount - fg.current_amount) as total_remaining_amount,
    AVG(fg.progress_percentage) as average_progress,
    SUM(CASE WHEN fg.priority = 'CRITICAL' THEN 1 ELSE 0 END) as critical_goals,
    SUM(CASE WHEN fg.priority = 'HIGH' THEN 1 ELSE 0 END) as high_priority_goals,
    SUM(CASE WHEN fg.target_date < CURRENT_DATE THEN 1 ELSE 0 END) as overdue_goals
FROM financial_goals fg
WHERE fg.status = 'ACTIVE'
GROUP BY fg.user_id;

-- View: Upcoming Recurring Transactions
CREATE OR REPLACE VIEW v_upcoming_recurring_transactions AS
SELECT 
    rt.user_id,
    rt.id,
    rt.name,
    rt.type,
    rt.amount,
    rt.frequency,
    rt.next_execution_date,
    rt.category,
    DATEDIFF(rt.next_execution_date, CURRENT_DATE) as days_until_next
FROM recurring_transactions rt
WHERE rt.status = 'ACTIVE'
  AND rt.next_execution_date IS NOT NULL
  AND rt.next_execution_date <= DATE_ADD(CURRENT_DATE, INTERVAL 30 DAY)
ORDER BY rt.next_execution_date;

-- View: Monthly Cash Flow Summary
CREATE OR REPLACE VIEW v_monthly_cash_flow_summary AS
SELECT 
    user_id,
    DATE_FORMAT(record_date, '%Y-%m-01') as month,
    SUM(total_income) as monthly_income,
    SUM(total_expenses) as monthly_expenses,
    SUM(net_cash_flow) as monthly_net_cash_flow,
    AVG(total_income) as avg_income,
    AVG(total_expenses) as avg_expenses,
    COUNT(*) as record_count
FROM cash_flow_records
WHERE period_type = 'MONTHLY'
GROUP BY user_id, DATE_FORMAT(record_date, '%Y-%m-01')
ORDER BY user_id, month DESC;

-- View: Document Statistics
CREATE OR REPLACE VIEW v_document_statistics AS
SELECT 
    user_id,
    COUNT(*) as total_documents,
    SUM(CASE WHEN is_active = TRUE THEN 1 ELSE 0 END) as active_documents,
    SUM(CASE WHEN is_verified = TRUE THEN 1 ELSE 0 END) as verified_documents,
    SUM(CASE WHEN expiry_date < CURRENT_TIMESTAMP THEN 1 ELSE 0 END) as expired_documents,
    SUM(CASE WHEN expiry_date BETWEEN CURRENT_TIMESTAMP AND DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY) THEN 1 ELSE 0 END) as expiring_soon,
    SUM(file_size) as total_storage_bytes,
    COUNT(DISTINCT document_type) as document_types_count,
    COUNT(DISTINCT category) as categories_count
FROM documents
GROUP BY user_id;

-- View: Latest Credit Scores by Provider
CREATE OR REPLACE VIEW v_latest_credit_scores AS
SELECT 
    cs1.user_id,
    cs1.provider,
    cs1.score as current_score,
    cs1.score_rating as current_rating,
    cs1.record_date as latest_record_date,
    cs2.score as previous_score,
    cs2.record_date as previous_record_date,
    cs1.score - COALESCE(cs2.score, cs1.score) as point_change,
    CASE 
        WHEN cs1.score > COALESCE(cs2.score, cs1.score) THEN 'IMPROVING'
        WHEN cs1.score < COALESCE(cs2.score, cs1.score) THEN 'DECLINING'
        ELSE 'STABLE'
    END as trend
FROM (
    SELECT cs.*
    FROM credit_scores cs
    INNER JOIN (
        SELECT user_id, provider, MAX(record_date) as max_date
        FROM credit_scores
        GROUP BY user_id, provider
    ) latest ON cs.user_id = latest.user_id 
        AND cs.provider = latest.provider 
        AND cs.record_date = latest.max_date
) cs1
LEFT JOIN (
    SELECT cs.*
    FROM credit_scores cs
    INNER JOIN (
        SELECT cs1.user_id, cs1.provider, MAX(cs2.record_date) as prev_date
        FROM credit_scores cs1
        INNER JOIN credit_scores cs2 
            ON cs1.user_id = cs2.user_id 
            AND cs1.provider = cs2.provider
            AND cs2.record_date < cs1.record_date
        INNER JOIN (
            SELECT user_id, provider, MAX(record_date) as max_date
            FROM credit_scores
            GROUP BY user_id, provider
        ) latest ON cs1.user_id = latest.user_id 
            AND cs1.provider = latest.provider 
            AND cs1.record_date = latest.max_date
        GROUP BY cs1.user_id, cs1.provider
    ) prev ON cs.user_id = prev.user_id 
        AND cs.provider = prev.provider 
        AND cs.record_date = prev.prev_date
) cs2 ON cs1.user_id = cs2.user_id AND cs1.provider = cs2.provider;

-- View: Goal Progress Tracking
CREATE OR REPLACE VIEW v_goal_progress_tracking AS
SELECT 
    fg.id,
    fg.user_id,
    fg.goal_name,
    fg.goal_type,
    fg.target_amount,
    fg.current_amount,
    fg.progress_percentage,
    DATEDIFF(fg.target_date, CURRENT_DATE) as days_remaining,
    DATEDIFF(CURRENT_DATE, fg.start_date) as days_elapsed,
    DATEDIFF(fg.target_date, fg.start_date) as total_days,
    CASE 
        WHEN fg.start_date IS NULL OR fg.target_date <= fg.start_date THEN 0
        ELSE CASE 
            WHEN (CAST(DATEDIFF(CURRENT_DATE, fg.start_date) AS DECIMAL(10,2)) / 
                NULLIF(CAST(DATEDIFF(fg.target_date, fg.start_date) AS DECIMAL(10,2)), 0) * 100
            ) <= (fg.progress_percentage + 10) THEN 1
            ELSE 0
        END
    END as is_on_track,
    (fg.target_amount - fg.current_amount) / 
        NULLIF(GREATEST(DATEDIFF(fg.target_date, CURRENT_DATE) / 30.0, 1), 0) as required_monthly_contribution
FROM financial_goals fg
WHERE fg.status = 'ACTIVE';

-- View: Recurring Transaction Summary
CREATE OR REPLACE VIEW v_recurring_transaction_summary AS
SELECT 
    user_id,
    type,
    category,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count,
    SUM(CASE WHEN auto_execute = TRUE THEN 1 ELSE 0 END) as auto_execute_count
FROM recurring_transactions
GROUP BY user_id, type, category
ORDER BY user_id, type, total_amount DESC;
