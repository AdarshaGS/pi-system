ALTER TABLE transactions
    ADD COLUMN transaction_id VARCHAR(100),
    ADD COLUMN type VARCHAR(20),
    ADD COLUMN category VARCHAR(50),
    ADD COLUMN merchant_name VARCHAR(255),
    ADD COLUMN receipt_url VARCHAR(500),
    ADD COLUMN error_code VARCHAR(50),
    ADD COLUMN error_message TEXT,
    ADD COLUMN completed_at TIMESTAMP NULL;

CREATE UNIQUE INDEX idx_transactions_transaction_id_unique
    ON transactions (transaction_id);

CREATE INDEX idx_transactions_status
    ON transactions (status);

ALTER TABLE transaction_requests
    ADD COLUMN responded_at TIMESTAMP NULL;