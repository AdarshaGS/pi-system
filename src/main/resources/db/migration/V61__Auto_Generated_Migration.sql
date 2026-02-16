-- Migration: Auto_Generated_Migration
-- Generated: 2026-02-14T12:29:16.922824
-- Changes: 20

-- Add new columns
ALTER TABLE etfs ADD COLUMN entity_type VARCHAR(50);

ALTER TABLE etfs ADD COLUMN price DOUBLE;

ALTER TABLE etfs ADD COLUMN description VARCHAR(255);

ALTER TABLE etfs ADD COLUMN total_expense_ratio DOUBLE;

-- Modify existing columns
ALTER TABLE etfs MODIFY COLUMN symbol VARCHAR(255);

-- Drop columns (handle with care!)
-- ALTER TABLE etfs DROP COLUMN isin;

-- ALTER TABLE etfs DROP COLUMN exchange;

-- ALTER TABLE etfs DROP COLUMN etf_type;

-- ALTER TABLE etfs DROP COLUMN underlying_index;

-- ALTER TABLE etfs DROP COLUMN fund_house;

-- ALTER TABLE etfs DROP COLUMN expense_ratio;

-- ALTER TABLE etfs DROP COLUMN aum;

-- ALTER TABLE etfs DROP COLUMN nav;

-- ALTER TABLE etfs DROP COLUMN market_price;

-- ALTER TABLE etfs DROP COLUMN price_date;

-- ALTER TABLE etfs DROP COLUMN tracking_error;

-- ALTER TABLE etfs DROP COLUMN dividend_yield;

-- ALTER TABLE etfs DROP COLUMN lot_size;

-- ALTER TABLE etfs DROP COLUMN created_date;

-- ALTER TABLE etfs DROP COLUMN updated_date;

