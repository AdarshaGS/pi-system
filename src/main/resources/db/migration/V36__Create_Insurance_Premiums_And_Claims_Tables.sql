-- V36__Create_Insurance_Premiums_And_Claims_Tables.sql
-- Description: Add premium payment tracking and claims management for insurance policies

-- Insurance Premiums Table
CREATE TABLE IF NOT EXISTS insurance_premiums (
    id BIGINT NOT NULL AUTO_INCREMENT,
    insurance_id BIGINT NOT NULL,
    
    payment_amount DECIMAL(15,2) NOT NULL,
    payment_date DATE NOT NULL,
    due_date DATE,
    
    payment_status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    
    is_auto_renewal BOOLEAN DEFAULT FALSE,
    notes TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_premium_insurance FOREIGN KEY (insurance_id) REFERENCES insurance_policies(id) ON DELETE CASCADE,
    INDEX idx_insurance_premiums_insurance_id (insurance_id),
    INDEX idx_insurance_premiums_payment_date (payment_date),
    INDEX idx_insurance_premiums_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insurance Claims Table
CREATE TABLE IF NOT EXISTS insurance_claims (
    id BIGINT NOT NULL AUTO_INCREMENT,
    insurance_id BIGINT NOT NULL,
    
    claim_number VARCHAR(100),
    claim_amount DECIMAL(15,2) NOT NULL,
    approved_amount DECIMAL(15,2),
    
    claim_date DATE NOT NULL,
    incident_date DATE,
    settlement_date DATE,
    
    claim_status VARCHAR(20) NOT NULL,
    claim_type VARCHAR(50),
    
    description TEXT,
    rejection_reason TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    CONSTRAINT fk_claim_insurance FOREIGN KEY (insurance_id) REFERENCES insurance_policies(id) ON DELETE CASCADE,
    INDEX idx_insurance_claims_insurance_id (insurance_id),
    INDEX idx_insurance_claims_claim_date (claim_date),
    INDEX idx_insurance_claims_claim_status (claim_status),
    INDEX idx_insurance_claims_claim_number (claim_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
