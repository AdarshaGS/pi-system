-- V69: Add sms_transaction_id back-link to expenses and incomes
-- Allows tracing any budget entry back to the originating SMS transaction.

ALTER TABLE expenses
    ADD COLUMN sms_transaction_id BIGINT NULL COMMENT 'FK to sms_transactions.id — null for manually created entries';

ALTER TABLE incomes
    ADD COLUMN sms_transaction_id BIGINT NULL COMMENT 'FK to sms_transactions.id — null for manually created entries';
