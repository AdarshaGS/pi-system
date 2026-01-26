-- Tax Details Table
CREATE TABLE tax_details (
   id BIGINT PRIMARY KEY AUTO_INCREMENT,
   user_id BIGINT NOT NULL,
   financial_year VARCHAR(255) NOT NULL,
   capital_gains_short_term DECIMAL(10,2),
   capital_gains_long_term DECIMAL(10,2),
   dividend_income DECIMAL(10,2),
   tax_paid DECIMAL(10,2),
   tax_payable DECIMAL(10,2),
   created_date DATE,
   CONSTRAINT fk_tax_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- External Services Table
CREATE TABLE external_services (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    service_name VARCHAR(255) NOT NULL
);

-- External Service Properties Table
CREATE TABLE external_service_properties (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    external_service_id BIGINT NOT NULL,
    CONSTRAINT fk_esp_es FOREIGN KEY (external_service_id) REFERENCES external_services(id)
);

-- Add Profit and Loss Percentage to Portfolio Holdings
ALTER TABLE portfolio_holdings ADD COLUMN profit_and_loss_percentage DECIMAL(19,4);
