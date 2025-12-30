ALTER TABLE stocks ADD COLUMN entity_type INT DEFAULT 1;
ALTER TABLE savings_account_details ADD COLUMN entity_type INT DEFAULT 7;
ALTER TABLE fixed_deposits ADD COLUMN entity_type INT DEFAULT 8;
ALTER TABLE recurring_deposits ADD COLUMN entity_type INT DEFAULT 9;
ALTER TABLE lending_records ADD COLUMN entity_type INT DEFAULT 5;
ALTER TABLE loans ADD COLUMN entity_type INT DEFAULT 10;
ALTER TABLE insurance_policies ADD COLUMN entity_type INT DEFAULT 11;

ALTER TABLE user_assets MODIFY COLUMN asset_type INT NOT NULL;
ALTER TABLE user_liabilities MODIFY COLUMN liability_type INT NOT NULL;
