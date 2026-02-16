-- =====================================================
-- Insurance Tracking Tables
-- Version: V37
-- Description: Life and Health Insurance Management
-- Created: February 2, 2026
-- =====================================================

-- Insurance Policies Master Table
CREATE TABLE IF NOT EXISTS insurance_policies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    policy_number VARCHAR(100) NOT NULL,
    policy_type VARCHAR(50) NOT NULL COMMENT 'LIFE, HEALTH, TERM, ENDOWMENT, ULIP, CRITICAL_ILLNESS',
    provider_name VARCHAR(200) NOT NULL,
    policy_name VARCHAR(300) NOT NULL,
    
    -- Coverage Details
    sum_assured DECIMAL(15, 2) NOT NULL,
    coverage_amount DECIMAL(15, 2) NOT NULL,
    bonus_amount DECIMAL(15, 2) DEFAULT 0.00,
    
    -- Premium Details
    premium_amount DECIMAL(12, 2) NOT NULL,
    premium_frequency VARCHAR(20) NOT NULL COMMENT 'MONTHLY, QUARTERLY, HALF_YEARLY, YEARLY',
    premium_paying_term INT NOT NULL COMMENT 'Years',
    policy_term INT NOT NULL COMMENT 'Years',
    
    -- Dates
    policy_start_date DATE NOT NULL,
    policy_end_date DATE NOT NULL,
    next_premium_date DATE,
    last_premium_paid_date DATE,
    
    -- Maturity Details
    maturity_amount DECIMAL(15, 2),
    maturity_date DATE,
    
    -- Nominees
    nominee_name VARCHAR(200),
    nominee_relation VARCHAR(50),
    nominee_dob DATE,
    
    -- Policy Status
    policy_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE, LAPSED, MATURED, SURRENDERED, CLAIMED',
    is_active BOOLEAN DEFAULT TRUE,
    
    -- Additional Info
    agent_name VARCHAR(200),
    agent_contact VARCHAR(20),
    plan_code VARCHAR(50),
    riders TEXT COMMENT 'JSON array of riders',
    exclusions TEXT,
    notes TEXT,
    
    -- Documents
    policy_document_url VARCHAR(500),
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_insurance_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_policy (user_id),
    INDEX idx_policy_type (policy_type),
    INDEX idx_policy_status (policy_status),
    INDEX idx_next_premium_date (next_premium_date),
    UNIQUE KEY unique_user_policy_number (user_id, policy_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Insurance policy master data';

-- Premium Payment History Table
CREATE TABLE IF NOT EXISTS insurance_premium_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    
    -- Payment Details
    payment_date DATE NOT NULL,
    premium_amount DECIMAL(12, 2) NOT NULL,
    payment_mode VARCHAR(50) COMMENT 'ONLINE, CHEQUE, CASH, AUTO_DEBIT',
    transaction_reference VARCHAR(100),
    
    -- Period Covered
    coverage_start_date DATE NOT NULL,
    coverage_end_date DATE NOT NULL,
    
    -- Status
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID' COMMENT 'PAID, PENDING, FAILED, REFUNDED',
    
    -- Late Payment
    is_late_payment BOOLEAN DEFAULT FALSE,
    late_fee DECIMAL(10, 2) DEFAULT 0.00,
    grace_period_used BOOLEAN DEFAULT FALSE,
    
    -- Additional Details
    receipt_number VARCHAR(100),
    notes TEXT,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_premium_payment_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_premium_payment_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies(id) ON DELETE CASCADE,
    INDEX idx_payment_user (user_id),
    INDEX idx_payment_policy (policy_id),
    INDEX idx_payment_date (payment_date),
    INDEX idx_payment_status (payment_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Insurance premium payment history';

-- Insurance Claims Table
CREATE TABLE IF NOT EXISTS insurance_claims (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    policy_id BIGINT NOT NULL,
    
    -- Claim Details
    claim_number VARCHAR(100) NOT NULL,
    claim_type VARCHAR(50) NOT NULL COMMENT 'DEATH, MATURITY, HOSPITALIZATION, CRITICAL_ILLNESS, ACCIDENT, DISABILITY, SURGERY',
    claim_amount DECIMAL(15, 2) NOT NULL,
    approved_amount DECIMAL(15, 2),
    
    -- Dates
    claim_date DATE NOT NULL,
    incident_date DATE,
    approval_date DATE,
    settlement_date DATE,
    
    -- Status
    claim_status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED' COMMENT 'SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, SETTLED, WITHDRAWN',
    
    -- Details
    reason TEXT NOT NULL,
    hospital_name VARCHAR(300),
    doctor_name VARCHAR(200),
    diagnosis TEXT,
    
    -- Settlement
    settlement_mode VARCHAR(50) COMMENT 'BANK_TRANSFER, CHEQUE, DIRECT_TO_HOSPITAL',
    settlement_reference VARCHAR(100),
    
    -- Documents
    claim_document_urls TEXT COMMENT 'JSON array of document URLs',
    
    -- Rejection Details
    rejection_reason TEXT,
    
    notes TEXT,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_claim_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_claim_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies(id) ON DELETE CASCADE,
    INDEX idx_claim_user (user_id),
    INDEX idx_claim_policy (policy_id),
    INDEX idx_claim_status (claim_status),
    INDEX idx_claim_date (claim_date),
    UNIQUE KEY unique_claim_number (claim_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Insurance claim tracking';

-- Policy Riders Table (Additional Benefits)
CREATE TABLE IF NOT EXISTS insurance_policy_riders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    
    -- Rider Details
    rider_name VARCHAR(200) NOT NULL,
    rider_type VARCHAR(50) NOT NULL COMMENT 'ACCIDENTAL_DEATH, CRITICAL_ILLNESS, WAIVER_OF_PREMIUM, INCOME_BENEFIT, DISABILITY',
    rider_sum_assured DECIMAL(15, 2) NOT NULL,
    rider_premium DECIMAL(10, 2) NOT NULL,
    
    -- Dates
    rider_start_date DATE NOT NULL,
    rider_end_date DATE NOT NULL,
    
    -- Status
    is_active BOOLEAN DEFAULT TRUE,
    
    notes TEXT,
    
    -- Audit fields
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rider_policy FOREIGN KEY (policy_id) REFERENCES insurance_policies(id) ON DELETE CASCADE,
    INDEX idx_rider_policy (policy_id),
    INDEX idx_rider_type (rider_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Insurance policy riders/add-ons';

-- =====================================================
-- Sample Data for Testing
-- =====================================================

-- Sample Life Insurance Policies
INSERT INTO insurance_policies (
    user_id, policy_number, policy_type, provider_name, policy_name,
    sum_assured, coverage_amount, premium_amount, premium_frequency,
    premium_paying_term, policy_term, policy_start_date, policy_end_date,
    next_premium_date, policy_status, nominee_name, nominee_relation
) VALUES
(1, 'LIC-123456789', 'TERM', 'LIC of India', 'Jeevan Anand Policy', 
 5000000.00, 5000000.00, 15000.00, 'YEARLY', 20, 20, '2020-01-15', '2040-01-15',
 '2026-01-15', 'ACTIVE', 'Spouse Name', 'Spouse'),
 
(1, 'HDFC-987654321', 'ENDOWMENT', 'HDFC Life', 'Click 2 Invest ULIP', 
 2000000.00, 2000000.00, 8000.00, 'MONTHLY', 10, 15, '2021-06-01', '2036-06-01',
 '2026-03-01', 'ACTIVE', 'Parent Name', 'Parent'),
 
(1, 'MAX-456789123', 'HEALTH', 'Max Bupa', 'Health Companion Individual', 
 500000.00, 500000.00, 12000.00, 'YEARLY', 1, 1, '2025-04-01', '2026-04-01',
 '2026-04-01', 'ACTIVE', 'Self', 'Self');

-- Sample Premium Payments
INSERT INTO insurance_premium_payments (
    user_id, policy_id, payment_date, premium_amount, payment_mode,
    coverage_start_date, coverage_end_date, payment_status, receipt_number
) VALUES
(1, 1, '2025-01-15', 15000.00, 'ONLINE', '2025-01-15', '2026-01-15', 'PAID', 'RCP-2025-001'),
(1, 2, '2026-01-01', 8000.00, 'AUTO_DEBIT', '2026-01-01', '2026-02-01', 'PAID', 'RCP-2026-002'),
(1, 2, '2026-02-01', 8000.00, 'AUTO_DEBIT', '2026-02-01', '2026-03-01', 'PAID', 'RCP-2026-003'),
(1, 3, '2025-04-01', 12000.00, 'ONLINE', '2025-04-01', '2026-04-01', 'PAID', 'RCP-2025-004');

-- Sample Policy Riders
INSERT INTO insurance_policy_riders (
    policy_id, rider_name, rider_type, rider_sum_assured, rider_premium,
    rider_start_date, rider_end_date, is_active
) VALUES
(1, 'Accidental Death Benefit', 'ACCIDENTAL_DEATH', 5000000.00, 500.00, '2020-01-15', '2040-01-15', TRUE),
(1, 'Critical Illness Rider', 'CRITICAL_ILLNESS', 1000000.00, 800.00, '2020-01-15', '2040-01-15', TRUE),
(3, 'Personal Accident Cover', 'ACCIDENTAL_DEATH', 1000000.00, 300.00, '2025-04-01', '2026-04-01', TRUE);
