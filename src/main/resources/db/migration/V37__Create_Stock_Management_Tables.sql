-- V37__Create_Stock_Management_Tables.sql
-- Description: Add comprehensive stock management features including price history, watchlist, alerts, and corporate actions

-- Stock Prices Table (Historical OHLC Data)
CREATE TABLE IF NOT EXISTS stock_prices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(50) NOT NULL,
    
    price_date DATE NOT NULL,
    open_price DECIMAL(15,2),
    high_price DECIMAL(15,2),
    low_price DECIMAL(15,2),
    close_price DECIMAL(15,2),
    volume BIGINT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_symbol_date (symbol, price_date),
    INDEX idx_stock_prices_symbol (symbol),
    INDEX idx_stock_prices_date (price_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Fundamentals Table
CREATE TABLE IF NOT EXISTS stock_fundamentals (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(50) NOT NULL,
    
    market_cap DECIMAL(20,2),
    pe_ratio DECIMAL(10,2),
    pb_ratio DECIMAL(10,2),
    dividend_yield DECIMAL(5,2),
    eps DECIMAL(10,2),
    roe DECIMAL(10,2),
    roa DECIMAL(10,2),
    
    week_52_high DECIMAL(15,2),
    week_52_low DECIMAL(15,2),
    
    book_value DECIMAL(15,2),
    face_value DECIMAL(10,2),
    
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_symbol (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Watchlist Table
CREATE TABLE IF NOT EXISTS stock_watchlist (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    notes TEXT,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_symbol (user_id, symbol),
    INDEX idx_watchlist_user_id (user_id),
    INDEX idx_watchlist_symbol (symbol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Price Alerts Table
CREATE TABLE IF NOT EXISTS price_alerts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    
    alert_type VARCHAR(20) NOT NULL,
    target_price DECIMAL(15,2),
    percentage_change DECIMAL(5,2),
    
    is_triggered BOOLEAN DEFAULT FALSE,
    triggered_at TIMESTAMP NULL,
    
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    INDEX idx_alerts_user_id (user_id),
    INDEX idx_alerts_symbol (symbol),
    INDEX idx_alerts_active (is_active),
    INDEX idx_alerts_triggered (is_triggered)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Corporate Actions Table
CREATE TABLE IF NOT EXISTS corporate_actions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    symbol VARCHAR(50) NOT NULL,
    
    action_type VARCHAR(20) NOT NULL,
    
    announcement_date DATE,
    ex_date DATE,
    record_date DATE,
    payment_date DATE,
    
    dividend_amount DECIMAL(10,2),
    split_ratio VARCHAR(20),
    bonus_ratio VARCHAR(20),
    rights_ratio VARCHAR(20),
    rights_price DECIMAL(15,2),
    
    description TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id),
    INDEX idx_corporate_actions_symbol (symbol),
    INDEX idx_corporate_actions_type (action_type),
    INDEX idx_corporate_actions_ex_date (ex_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
