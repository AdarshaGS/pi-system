CREATE TABLE scheduled_jobs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_name VARCHAR(255) NOT NULL UNIQUE,
    job_description TEXT,
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    cron_expression VARCHAR(255),
    last_run_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Seed existing jobs
INSERT INTO scheduled_jobs (job_name, job_description, is_enabled, cron_expression) VALUES
('BUDGET_ALERTS', 'Checks budgets and generates alerts for overspending', TRUE, '0 0 21 * * *'),
('BUDGET_RECURRING_TRANSACTIONS', 'Generates transactions from recurring templates in budget module', TRUE, '0 0 1 * * *'),
('INVESTMENT_RECURRING_TRANSACTIONS', 'Processes due investment transactions from templates', TRUE, '0 0 1 * * *'),
('LENDING_DUE_DATE_CHECK', 'Checks for overdue and due-today lending records', TRUE, '0 0 10 * * ?'),
('SUBSCRIPTION_REMINDERS', 'Sends reminders for upcoming subscription renewals', TRUE, '0 0 8 * * *'),
('STOCK_PRICE_UPDATE', 'Updates stock prices from third-party APIs', FALSE, '0 0/1 * * * ?'),
('ALERT_PROCESSOR', 'Processes and sends pending alerts from the central alert system', TRUE, '0 0/5 * * * *');
