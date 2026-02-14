-- Migration: Auto_Generated_Migration
-- Generated: 2026-02-13T22:46:48.436143
-- Changes: 70

-- Create new tables
CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    symbol VARCHAR(255),
    target_price DECIMAL(19, 2),
    price_condition VARCHAR(255),
    days_before_due INT,
    percentage_change DECIMAL(19, 2),
    channel VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    last_triggered_at TIMESTAMP,
    description VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AlertRule';

CREATE TABLE IF NOT EXISTS user_notifications (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(1000),
    type VARCHAR(50) NOT NULL,
    is_read BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    read_at TIMESTAMP,
    meta_value VARCHAR(255),
    alert_rule_id BIGINT,
    channel VARCHAR(50),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UserNotification';

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    service_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    amount DECIMAL(10, 2) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    next_renewal_date DATE,
    cancellation_date DATE,
    status VARCHAR(50) NOT NULL,
    auto_renewal BOOLEAN NOT NULL,
    payment_method VARCHAR(50),
    reminder_days_before INT NOT NULL,
    last_used_date DATE,
    notes VARCHAR(1000),
    website_url VARCHAR(300),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Subscription';

CREATE TABLE IF NOT EXISTS feature_config (
    id BIGINT AUTO_INCREMENT NOT NULL,
    feature_flag VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL,
    enabled_for_all BOOLEAN NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50),
    requires_subscription BOOLEAN NOT NULL,
    min_subscription_tier VARCHAR(50),
    beta_feature BOOLEAN NOT NULL,
    enabled_since TIMESTAMP,
    disabled_since TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FeatureConfig';

CREATE TABLE IF NOT EXISTS etf_holdings (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    etf_id VARCHAR(255),
    total_quantity INT NOT NULL,
    average_price DECIMAL(15, 4) NOT NULL,
    invested_amount DECIMAL(15, 2) NOT NULL,
    current_price DECIMAL(15, 4),
    current_value DECIMAL(15, 2),
    unrealized_gain DECIMAL(15, 2),
    unrealized_gain_percentage DECIMAL(10, 2),
    last_updated TIMESTAMP,
    created_date TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETFHolding';
ALTER TABLE etf_holdings ADD CONSTRAINT unique_s_etf UNIQUE (user_id, etf_id);

CREATE TABLE IF NOT EXISTS etf_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    etf_id VARCHAR(255),
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(15, 4) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    brokerage DECIMAL(10, 2),
    stt DECIMAL(10, 2),
    stamp_duty DECIMAL(10, 2),
    transaction_charges DECIMAL(10, 2),
    gst DECIMAL(10, 2),
    total_charges DECIMAL(10, 2),
    net_amount DECIMAL(15, 2) NOT NULL,
    exchange VARCHAR(10) NOT NULL,
    order_id VARCHAR(50),
    notes VARCHAR(255),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ETFTransaction';

CREATE TABLE IF NOT EXISTS cash_flow_records (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    total_income DECIMAL(15, 2) NOT NULL,
    total_expenses DECIMAL(15, 2) NOT NULL,
    net_cash_flow DECIMAL(15, 2) NOT NULL,
    salary_income DECIMAL(15, 2),
    investment_income DECIMAL(15, 2),
    business_income DECIMAL(15, 2),
    other_income DECIMAL(15, 2),
    housing_expenses DECIMAL(15, 2),
    transportation_expenses DECIMAL(15, 2),
    food_expenses DECIMAL(15, 2),
    utilities_expenses DECIMAL(15, 2),
    entertainment_expenses DECIMAL(15, 2),
    healthcare_expenses DECIMAL(15, 2),
    education_expenses DECIMAL(15, 2),
    debt_payments DECIMAL(15, 2),
    savings_contributions DECIMAL(15, 2),
    investment_contributions DECIMAL(15, 2),
    other_expenses DECIMAL(15, 2),
    period_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CashFlowRecord';

CREATE TABLE IF NOT EXISTS corporate_actions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    announcement_date DATE,
    ex_date DATE,
    record_date DATE,
    payment_date DATE,
    dividend_amount DECIMAL(10, 2),
    split_ratio VARCHAR(20),
    bonus_ratio VARCHAR(20),
    rights_ratio VARCHAR(20),
    rights_price DECIMAL(15, 2),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CorporateAction';

CREATE TABLE IF NOT EXISTS credit_scores (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    score INT NOT NULL,
    provider VARCHAR(50) NOT NULL,
    record_date TIMESTAMP NOT NULL,
    score_rating VARCHAR(20),
    change_from_previous INT,
    factors VARCHAR(1000),
    recommendations VARCHAR(1000),
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CreditScore';

CREATE TABLE IF NOT EXISTS documents (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(1000),
    tags VARCHAR(500),
    related_entity_id BIGINT,
    related_entity_type VARCHAR(50),
    uploaded_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    expiry_date TIMESTAMP,
    is_encrypted BOOLEAN,
    encryption_key VARCHAR(100),
    is_verified BOOLEAN,
    verified_at TIMESTAMP,
    verified_by VARCHAR(255),
    checksum VARCHAR(100),
    version INT,
    previous_version_id BIGINT,
    is_active BOOLEAN,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Document';

CREATE TABLE IF NOT EXISTS financial_goals (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    goal_name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    goal_type VARCHAR(50) NOT NULL,
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) NOT NULL,
    target_date DATE NOT NULL,
    start_date DATE,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    expected_return_rate DECIMAL(5, 2),
    monthly_contribution DECIMAL(15, 2),
    linked_accounts VARCHAR(500),
    auto_contribute BOOLEAN,
    reminder_day_of_month INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    progress_percentage DECIMAL(5, 2),
    notes VARCHAR(500),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='FinancialGoal';

CREATE TABLE IF NOT EXISTS goal_milestones (
    id BIGINT AUTO_INCREMENT NOT NULL,
    goal_id BIGINT NOT NULL,
    milestone_name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    target_amount DECIMAL(15, 2) NOT NULL,
    target_date DATE NOT NULL,
    achieved_date DATE,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='GoalMilestone';

CREATE TABLE IF NOT EXISTS portfolio_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    fees DECIMAL(15, 2),
    total_amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    notes VARCHAR(255),
    realized_gain DECIMAL(15, 2),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PortfolioTransaction';
CREATE INDEX idx_user_id ON portfolio_transactions (user_id);
CREATE INDEX idx_symbol ON portfolio_transactions (symbol);
CREATE INDEX idx_transaction_date ON portfolio_transactions (transaction_date);
CREATE INDEX idx_user_symbol ON portfolio_transactions (user_id, symbol);

CREATE TABLE IF NOT EXISTS price_alerts (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    target_price DECIMAL(15, 2),
    percentage_change DECIMAL(5, 2),
    is_triggered BOOLEAN,
    triggered_at TIMESTAMP,
    is_active BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PriceAlert';

CREATE TABLE IF NOT EXISTS recurring_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(50) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    frequency VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_execution_date DATE,
    last_execution_date DATE,
    status VARCHAR(50) NOT NULL,
    category VARCHAR(100),
    source_account VARCHAR(100),
    destination_account VARCHAR(100),
    day_of_month INT,
    day_of_week INT,
    auto_execute BOOLEAN,
    send_reminder BOOLEAN,
    reminder_days_before INT,
    execution_count INT NOT NULL,
    max_executions INT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    notes VARCHAR(500),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RecurringTransaction';

CREATE TABLE IF NOT EXISTS recurring_transaction_history (
    id BIGINT AUTO_INCREMENT NOT NULL,
    recurring_transaction_id BIGINT NOT NULL,
    executed_at TIMESTAMP NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message VARCHAR(500),
    transaction_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RecurringTransactionHistory';

CREATE TABLE IF NOT EXISTS stock_fundamentals (
    id BIGINT AUTO_INCREMENT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    market_cap DECIMAL(20, 2),
    pe_ratio DECIMAL(10, 2),
    pb_ratio DECIMAL(10, 2),
    dividend_yield DECIMAL(5, 2),
    eps DECIMAL(10, 2),
    roe DECIMAL(10, 2),
    roa DECIMAL(10, 2),
    week_52_high DECIMAL(15, 2),
    week_52_low DECIMAL(15, 2),
    book_value DECIMAL(15, 2),
    face_value DECIMAL(10, 2),
    last_updated TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='StockFundamentals';

CREATE TABLE IF NOT EXISTS stock_prices (
    id BIGINT AUTO_INCREMENT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    price_date DATE NOT NULL,
    open_price DECIMAL(15, 2),
    high_price DECIMAL(15, 2),
    low_price DECIMAL(15, 2),
    close_price DECIMAL(15, 2),
    volume BIGINT,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='StockPrice';

CREATE TABLE IF NOT EXISTS stock_watchlist (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    notes VARCHAR(255),
    added_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='StockWatchlist';

CREATE TABLE IF NOT EXISTS loan_payments (
    id BIGINT AUTO_INCREMENT NOT NULL,
    loan_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    payment_amount DECIMAL(0, 0) NOT NULL,
    principal_paid DECIMAL(0, 0) NOT NULL,
    interest_paid DECIMAL(0, 0) NOT NULL,
    outstanding_balance DECIMAL(0, 0) NOT NULL,
    payment_type VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(255),
    transaction_reference VARCHAR(255),
    notes VARCHAR(255),
    created_at DATE NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='LoanPayment';

CREATE TABLE IF NOT EXISTS mutual_funds (
    id BIGINT AUTO_INCREMENT NOT NULL,
    scheme_code VARCHAR(50) NOT NULL,
    scheme_name VARCHAR(500) NOT NULL,
    fund_house VARCHAR(255) NOT NULL,
    scheme_type VARCHAR(100),
    scheme_category VARCHAR(100),
    nav DECIMAL(15, 4),
    nav_date DATE,
    expense_ratio DECIMAL(5, 2),
    aum DECIMAL(20, 2),
    min_investment DECIMAL(15, 2),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MutualFund';

CREATE TABLE IF NOT EXISTS mutual_fund_holdings (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    mutual_fund_id VARCHAR(255),
    folio_number VARCHAR(50),
    total_units DECIMAL(15, 4) NOT NULL,
    average_nav DECIMAL(15, 4) NOT NULL,
    invested_amount DECIMAL(15, 2) NOT NULL,
    current_nav DECIMAL(15, 4),
    current_value DECIMAL(15, 2),
    unrealized_gain DECIMAL(15, 2),
    unrealized_gain_percentage DECIMAL(10, 2),
    last_updated TIMESTAMP,
    created_date TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MutualFundHolding';
ALTER TABLE mutual_fund_holdings ADD CONSTRAINT unique_user_fund_folio UNIQUE (user_id, mutual_fund_id, folio_number);

CREATE TABLE IF NOT EXISTS mutual_fund_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    mutual_fund_id VARCHAR(255),
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date DATE NOT NULL,
    units DECIMAL(15, 4) NOT NULL,
    nav DECIMAL(15, 4) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    stamp_duty DECIMAL(10, 2),
    transaction_charges DECIMAL(10, 2),
    stt DECIMAL(10, 2),
    folio_number VARCHAR(50),
    notes VARCHAR(255),
    created_date TIMESTAMP,
    updated_date TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MutualFundTransaction';

CREATE TABLE IF NOT EXISTS upi_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    payer_vpa VARCHAR(255) NOT NULL,
    payee_vpa VARCHAR(255) NOT NULL,
    amount DOUBLE NOT NULL,
    remarks VARCHAR(255) NOT NULL,
    transaction_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    transaction_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UPITransaction';

CREATE TABLE IF NOT EXISTS insurance_claims (
    id BIGINT AUTO_INCREMENT NOT NULL,
    insurance_id BIGINT NOT NULL,
    claim_number VARCHAR(100),
    claim_amount DECIMAL(15, 2) NOT NULL,
    approved_amount DECIMAL(15, 2),
    claim_date DATE NOT NULL,
    incident_date DATE,
    settlement_date DATE,
    claim_status VARCHAR(50) NOT NULL,
    claim_type VARCHAR(50),
    description VARCHAR(255),
    rejection_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='InsuranceClaim';

CREATE TABLE IF NOT EXISTS insurance_premiums (
    id BIGINT AUTO_INCREMENT NOT NULL,
    insurance_id BIGINT NOT NULL,
    payment_amount DECIMAL(15, 2) NOT NULL,
    payment_date DATE NOT NULL,
    due_date DATE,
    payment_status VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50),
    transaction_reference VARCHAR(100),
    is_auto_renewal BOOLEAN,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='InsurancePremium';

CREATE TABLE IF NOT EXISTS capital_gains_transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    asset_type VARCHAR(50) NOT NULL,
    asset_name VARCHAR(255) NOT NULL,
    asset_symbol VARCHAR(50),
    quantity DECIMAL(20, 4) NOT NULL,
    purchase_date DATE NOT NULL,
    purchase_price DECIMAL(20, 2) NOT NULL,
    sale_date DATE NOT NULL,
    sale_price DECIMAL(20, 2) NOT NULL,
    purchase_value DECIMAL(20, 2) NOT NULL,
    sale_value DECIMAL(20, 2) NOT NULL,
    expenses DECIMAL(20, 2),
    indexed_cost DECIMAL(20, 2),
    holding_period_days INT NOT NULL,
    gain_type VARCHAR(20) NOT NULL,
    capital_gain DECIMAL(20, 2) NOT NULL,
    tax_rate DECIMAL(5, 2),
    tax_amount DECIMAL(20, 2),
    financial_year VARCHAR(10) NOT NULL,
    is_set_off BOOLEAN,
    set_off_amount DECIMAL(20, 2),
    notes VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CapitalGainsTransaction';
CREATE INDEX idx_user_financial_year ON capital_gains_transactions (user_id, financial_year);
CREATE INDEX idx_asset_type ON capital_gains_transactions (asset_type);
CREATE INDEX idx_gain_type ON capital_gains_transactions (gain_type);
CREATE INDEX idx_sale_date ON capital_gains_transactions (sale_date);

CREATE TABLE IF NOT EXISTS tds_entries (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    quarter INT NOT NULL,
    deductor_name VARCHAR(255) NOT NULL,
    deductor_tan VARCHAR(10) NOT NULL,
    deductor_pan VARCHAR(10),
    section VARCHAR(20) NOT NULL,
    income_type VARCHAR(100) NOT NULL,
    amount_paid DECIMAL(20, 2) NOT NULL,
    tds_deducted DECIMAL(20, 2) NOT NULL,
    tds_deposited_date DATE,
    certificate_number VARCHAR(50),
    certificate_date DATE,
    reconciliation_status VARCHAR(50),
    form_26as_amount DECIMAL(20, 2),
    difference_amount DECIMAL(20, 2),
    is_matched_with_26as BOOLEAN,
    matched_on TIMESTAMP,
    is_claimed_in_itr BOOLEAN,
    itr_acknowledgement_number VARCHAR(50),
    remarks VARCHAR(255),
    uploaded_certificate_path VARCHAR(500),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TDSEntry';
CREATE INDEX idx_user_financial_year ON tds_entries (user_id, financial_year);
CREATE INDEX idx_quarter ON tds_entries (quarter);
CREATE INDEX idx_deductor_tan ON tds_entries (deductor_tan);
CREATE INDEX idx_reconciliation_status ON tds_entries (reconciliation_status);
CREATE INDEX idx_section ON tds_entries (section);
CREATE INDEX idx_tds_deposited_date ON tds_entries (tds_deposited_date);

CREATE TABLE IF NOT EXISTS tax_saving_investments (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id BIGINT NOT NULL,
    investment_type VARCHAR(50) NOT NULL,
    category VARCHAR(100) NOT NULL,
    investment_name VARCHAR(255) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    investment_date DATE NOT NULL,
    financial_year VARCHAR(10) NOT NULL,
    linked_entity_type VARCHAR(50),
    linked_entity_id BIGINT,
    policy_number VARCHAR(100),
    maturity_date DATE,
    interest_rate DECIMAL(5, 2),
    self_or_family VARCHAR(20),
    is_senior_citizen BOOLEAN,
    donation_mode VARCHAR(50),
    pan_of_donee VARCHAR(10),
    is_100_percent_deduction BOOLEAN,
    is_auto_populated BOOLEAN,
    auto_populated_from VARCHAR(100),
    has_proof BOOLEAN,
    proof_uploaded BOOLEAN,
    notes VARCHAR(255),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TaxSavingInvestment';
CREATE INDEX idx_user_financial_year ON tax_saving_investments (user_id, financial_year);
CREATE INDEX idx_investment_type ON tax_saving_investments (investment_type);
CREATE INDEX idx_linked_entity ON tax_saving_investments (linked_entity_type, linked_entity_id);
CREATE INDEX idx_investment_date ON tax_saving_investments (investment_date);

CREATE TABLE IF NOT EXISTS bank_accounts (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    account_number VARCHAR(255),
    ifsc_code VARCHAR(255),
    bank_name VARCHAR(255),
    is_primary BOOLEAN,
    balance DOUBLE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BankAccount';

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT NOT NULL,
    transaction_id VARCHAR(255),
    sender_upi_id VARCHAR(255),
    receiver_upi_id VARCHAR(255),
    amount DECIMAL(19, 2),
    status VARCHAR(255),
    type VARCHAR(255),
    remarks VARCHAR(255),
    category VARCHAR(255),
    merchant_name VARCHAR(255),
    receipt_url VARCHAR(255),
    error_code VARCHAR(255),
    error_message VARCHAR(255),
    created_at TIMESTAMP,
    completed_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Transaction';

CREATE TABLE IF NOT EXISTS transaction_requests (
    id BIGINT AUTO_INCREMENT NOT NULL,
    requester_upi_id VARCHAR(255),
    payer_upi_id VARCHAR(255),
    amount DECIMAL(19, 2),
    status VARCHAR(255),
    remarks VARCHAR(255),
    created_at TIMESTAMP,
    responded_at TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TransactionRequest';

CREATE TABLE IF NOT EXISTS upi_ids (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    upi_id VARCHAR(255),
    is_merchant BOOLEAN,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UpiId';

CREATE TABLE IF NOT EXISTS upi_pins (
    id BIGINT AUTO_INCREMENT NOT NULL,
    user_id VARCHAR(255),
    pin_hash VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='UpiPin';

-- Add new columns
ALTER TABLE incomes ADD COLUMN description VARCHAR(500);

ALTER TABLE etfs ADD COLUMN isin VARCHAR(20);

ALTER TABLE etfs ADD COLUMN exchange VARCHAR(10) NOT NULL;

ALTER TABLE etfs ADD COLUMN etf_type VARCHAR(50) NOT NULL;

ALTER TABLE etfs ADD COLUMN underlying_index VARCHAR(100);

ALTER TABLE etfs ADD COLUMN fund_house VARCHAR(255) NOT NULL;

ALTER TABLE etfs ADD COLUMN expense_ratio DECIMAL(5, 2);

ALTER TABLE etfs ADD COLUMN aum DECIMAL(20, 2);

ALTER TABLE etfs ADD COLUMN nav DECIMAL(15, 4);

ALTER TABLE etfs ADD COLUMN market_price DECIMAL(15, 4);

ALTER TABLE etfs ADD COLUMN price_date DATE;

ALTER TABLE etfs ADD COLUMN tracking_error DECIMAL(5, 2);

ALTER TABLE etfs ADD COLUMN dividend_yield DECIMAL(5, 2);

ALTER TABLE etfs ADD COLUMN lot_size INT;

ALTER TABLE etfs ADD COLUMN created_date TIMESTAMP;

ALTER TABLE etfs ADD COLUMN updated_date TIMESTAMP;

ALTER TABLE tax_details ADD COLUMN gross_salary DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN standard_deduction DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN section_80c_deductions DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN section_80d_deductions DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN other_deductions DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN house_property_income DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN business_income DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN other_income DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN tds_deducted DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN advance_tax_paid DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN self_assessment_tax DECIMAL(0, 0);

ALTER TABLE tax_details ADD COLUMN selected_regime VARCHAR(50);

ALTER TABLE tax_details ADD COLUMN updated_date DATE;

ALTER TABLE users ADD COLUMN subscription_tier VARCHAR(50);

-- Modify existing columns
ALTER TABLE etfs MODIFY COLUMN symbol VARCHAR(20) NOT NULL;

-- Drop columns (handle with care!)
-- ALTER TABLE incomes DROP COLUMN notes;

-- ALTER TABLE etfs DROP COLUMN entity_type;

-- ALTER TABLE etfs DROP COLUMN price;

-- ALTER TABLE etfs DROP COLUMN description;

-- ALTER TABLE etfs DROP COLUMN total_expense_ratio;

