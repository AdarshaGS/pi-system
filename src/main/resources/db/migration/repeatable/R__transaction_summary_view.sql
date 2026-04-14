-- ============================================================================
-- REPEATABLE MIGRATION: Transaction Summary View
--
-- This view can be modified and re-executed whenever needed.
-- Flyway will detect changes via checksum and re-run automatically.
-- ============================================================================

CREATE OR REPLACE VIEW transaction_summary AS
SELECT
    e.user_id,
    DATE_FORMAT(e.expense_date, '%Y-%m') as month,
    e.category,
    COUNT(e.id) as transaction_count,
    SUM(e.amount) as total_amount,
    AVG(e.amount) as average_amount,
    MIN(e.amount) as min_amount,
    MAX(e.amount) as max_amount
FROM expenses e
GROUP BY e.user_id, DATE_FORMAT(e.expense_date, '%Y-%m'), e.category
ORDER BY e.user_id, month DESC, total_amount DESC;
