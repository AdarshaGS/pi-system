-- V46: Create Portfolio Transactions Table
-- Migration to support transaction management (Buy, Sell, Dividend, Bonus, Split, Merger)
-- Enables full portfolio tracking with realized gains calculation

CREATE TABLE IF NOT EXISTS portfolio_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('BUY', 'SELL', 'DIVIDEND', 'BONUS', 'SPLIT', 'MERGER')),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    fees DECIMAL(15, 2) DEFAULT 0.00 CHECK (fees >= 0),
    total_amount DECIMAL(15, 2) NOT NULL,
    transaction_date DATE NOT NULL,
    notes TEXT,
    realized_gain DECIMAL(15, 2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_portfolio_transaction_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for performance optimization
CREATE INDEX idx_portfolio_tx_user_id ON portfolio_transactions(user_id);
CREATE INDEX idx_portfolio_tx_symbol ON portfolio_transactions(symbol);
CREATE INDEX idx_portfolio_tx_transaction_date ON portfolio_transactions(transaction_date);
CREATE INDEX idx_portfolio_tx_user_symbol ON portfolio_transactions(user_id, symbol);
CREATE INDEX idx_portfolio_tx_type ON portfolio_transactions(transaction_type);

-- Create composite index for FIFO calculations
CREATE INDEX idx_portfolio_tx_user_symbol_type_date ON portfolio_transactions(user_id, symbol, transaction_type, transaction_date);