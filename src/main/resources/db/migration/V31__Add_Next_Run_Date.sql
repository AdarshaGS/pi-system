-- V31: Add next_run_date to recurring_templates table
-- Author: System
-- Date: 2026-02-01

ALTER TABLE recurring_templates 
ADD COLUMN next_run_date DATE AFTER last_generated;

-- Set initial next_run_date for existing active templates
UPDATE recurring_templates 
SET next_run_date = CASE 
    WHEN last_generated IS NOT NULL THEN DATE_ADD(last_generated, INTERVAL 1 DAY)
    ELSE start_date
END
WHERE is_active = TRUE AND next_run_date IS NULL;

-- Create index for efficient queries
CREATE INDEX idx_next_run_date ON recurring_templates(next_run_date, is_active);
CREATE INDEX idx_user_active_next_run ON recurring_templates(user_id, is_active, next_run_date);
