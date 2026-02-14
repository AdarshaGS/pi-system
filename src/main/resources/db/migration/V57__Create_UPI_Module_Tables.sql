-- UPI ID Table
CREATE TABLE upi_ids (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    upi_id VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bank Account Table
CREATE TABLE bank_accounts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    account_number VARCHAR(30) NOT NULL,
    ifsc_code VARCHAR(20) NOT NULL,
    bank_name VARCHAR(100),
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UPI PIN Table
CREATE TABLE upi_pins (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    pin_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Table
CREATE TABLE transactions (
    id SERIAL PRIMARY KEY,
    sender_upi_id VARCHAR(100) NOT NULL,
    receiver_upi_id VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- pending, success, failed
    remarks VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Request Table
CREATE TABLE transaction_requests (
    id SERIAL PRIMARY KEY,
    requester_upi_id VARCHAR(100) NOT NULL,
    payer_upi_id VARCHAR(100) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL, -- pending, accepted, rejected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Receipt Table
CREATE TABLE transaction_receipts (
    id SERIAL PRIMARY KEY,
    transaction_id INTEGER REFERENCES transactions(id),
    receipt_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);