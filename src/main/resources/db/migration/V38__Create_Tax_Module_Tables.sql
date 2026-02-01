-- V38: Create Tax Module Tables
-- Creates capital_gains_transactions, tax_saving_investments, and tds_entries tables

-- =====================================================================
-- Capital Gains Transactions Table
-- =====================================================================
CREATE TABLE capital_gains_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_type VARCHAR(50) NOT NULL COMMENT 'STOCK, MUTUAL_FUND, REAL_ESTATE, GOLD, BOND',
    asset_name VARCHAR(255) NOT NULL,
    asset_symbol VARCHAR(50) COMMENT 'For stocks/MF - symbol/ISIN',
    quantity DECIMAL(20, 4) NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_price DECIMAL(20, 2) NOT NULL,
    sale_date DATE NOT NULL,
    sale_price DECIMAL(20, 2) NOT NULL,
    purchase_value DECIMAL(20, 2) NOT NULL COMMENT 'Quantity * Purchase Price',
    sale_value DECIMAL(20, 2) NOT NULL COMMENT 'Quantity * Sale Price',
    expenses DECIMAL(20, 2) DEFAULT 0.00 COMMENT 'Brokerage, STT, etc.',
    indexed_cost DECIMAL(20, 2) COMMENT 'For LTCG - indexed cost of acquisition',
    holding_period_days INT NOT NULL,
    gain_type VARCHAR(20) NOT NULL COMMENT 'STCG or LTCG',
    capital_gain DECIMAL(20, 2) NOT NULL,
    tax_rate DECIMAL(5, 2) COMMENT 'Applicable tax rate %',
    tax_amount DECIMAL(20, 2) COMMENT 'Computed tax amount',
    financial_year VARCHAR(10) NOT NULL COMMENT 'FY in which sale occurred (e.g., 2025-26)',
    is_set_off BOOLEAN DEFAULT FALSE COMMENT 'Whether loss is set off',
    set_off_amount DECIMAL(20, 2) DEFAULT 0.00,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_financial_year (user_id, financial_year),
    INDEX idx_asset_type (asset_type),
    INDEX idx_gain_type (gain_type),
    INDEX idx_sale_date (sale_date),
    
    CONSTRAINT fk_cgt_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Tax Saving Investments Table
-- =====================================================================
CREATE TABLE tax_saving_investments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    investment_type VARCHAR(50) NOT NULL COMMENT '80C, 80D, 80E, 80G, 80CCD1B, 24B',
    category VARCHAR(100) NOT NULL COMMENT 'PPF, ELSS, LIC, NSC, MEDICLAIM, etc.',
    investment_name VARCHAR(255) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    investment_date DATE NOT NULL,
    financial_year VARCHAR(10) NOT NULL COMMENT 'e.g., 2025-26',
    
    -- Linking to actual records
    linked_entity_type VARCHAR(50) COMMENT 'INSURANCE, FD, LOAN, MUTUAL_FUND, etc.',
    linked_entity_id BIGINT COMMENT 'ID of the linked entity',
    
    -- Specific fields for different sections
    policy_number VARCHAR(100) COMMENT 'For insurance',
    maturity_date DATE COMMENT 'For FD, insurance',
    interest_rate DECIMAL(5, 2) COMMENT 'For FD, NSC',
    
    -- 80D specific
    self_or_family VARCHAR(20) COMMENT 'SELF, PARENT - for 80D',
    is_senior_citizen BOOLEAN DEFAULT FALSE COMMENT 'For 80D - different limits',
    
    -- 80G specific
    donation_mode VARCHAR(50) COMMENT 'CASH, CHEQUE, ONLINE - for 80G',
    pan_of_donee VARCHAR(10) COMMENT 'For 80G donations',
    is_100_percent_deduction BOOLEAN DEFAULT FALSE COMMENT '80G - 100% or 50%',
    
    -- Auto-population tracking
    is_auto_populated BOOLEAN DEFAULT FALSE,
    auto_populated_from VARCHAR(100) COMMENT 'Source of auto-population',
    
    -- Verification
    has_proof BOOLEAN DEFAULT FALSE,
    proof_uploaded BOOLEAN DEFAULT FALSE,
    
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_financial_year (user_id, financial_year),
    INDEX idx_investment_type (investment_type),
    INDEX idx_linked_entity (linked_entity_type, linked_entity_id),
    INDEX idx_investment_date (investment_date),
    
    CONSTRAINT fk_tsi_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- TDS Entries Table
-- =====================================================================
CREATE TABLE tds_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL COMMENT 'e.g., 2025-26',
    quarter INT NOT NULL COMMENT '1, 2, 3, or 4',
    
    -- Deductor details
    deductor_name VARCHAR(255) NOT NULL,
    deductor_tan VARCHAR(10) NOT NULL COMMENT 'Tax Deduction Account Number',
    deductor_pan VARCHAR(10) COMMENT 'PAN of deductor',
    
    -- TDS details
    section VARCHAR(20) NOT NULL COMMENT '192, 194A, 194C, 194H, 194J, etc.',
    income_type VARCHAR(100) NOT NULL COMMENT 'SALARY, INTEREST, PROFESSIONAL, etc.',
    amount_paid DECIMAL(20, 2) NOT NULL COMMENT 'Gross amount paid',
    tds_deducted DECIMAL(20, 2) NOT NULL,
    tds_deposited_date DATE COMMENT 'Date when TDS was deposited',
    
    -- Certificate details
    certificate_number VARCHAR(50),
    certificate_date DATE,
    
    -- Reconciliation
    reconciliation_status VARCHAR(50) DEFAULT 'PENDING' COMMENT 'PENDING, MATCHED, MISMATCHED, MISSING',
    form_26as_amount DECIMAL(20, 2) COMMENT 'Amount as per Form 26AS',
    difference_amount DECIMAL(20, 2) DEFAULT 0.00 COMMENT 'Difference between claimed and 26AS',
    
    -- Matching details
    is_matched_with_26as BOOLEAN DEFAULT FALSE,
    matched_on TIMESTAMP,
    
    -- Claim status
    is_claimed_in_itr BOOLEAN DEFAULT FALSE,
    itr_acknowledgement_number VARCHAR(50),
    
    -- Additional info
    remarks TEXT,
    uploaded_certificate_path VARCHAR(500),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_user_financial_year (user_id, financial_year),
    INDEX idx_quarter (quarter),
    INDEX idx_deductor_tan (deductor_tan),
    INDEX idx_reconciliation_status (reconciliation_status),
    INDEX idx_section (section),
    INDEX idx_tds_deposited_date (tds_deposited_date),
    
    CONSTRAINT fk_tds_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add indexes for common queries
CREATE INDEX idx_cgt_user_fy_gain ON capital_gains_transactions(user_id, financial_year, gain_type);
CREATE INDEX idx_tsi_user_fy_type ON tax_saving_investments(user_id, financial_year, investment_type);
CREATE INDEX idx_tds_user_fy_status ON tds_entries(user_id, financial_year, reconciliation_status);
