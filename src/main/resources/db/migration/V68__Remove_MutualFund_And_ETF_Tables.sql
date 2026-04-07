-- V68__Remove_MutualFund_And_ETF_Tables.sql
-- Remove Mutual Fund and ETF tables and related data

-- Drop Mutual Fund Tables
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS mutual_fund_holdings;
DROP TABLE IF EXISTS mutual_fund_transactions;
DROP TABLE IF EXISTS mutual_funds;

-- Drop ETF Tables
DROP TABLE IF EXISTS etf_holdings;
DROP TABLE IF EXISTS etf_transactions;
DROP TABLE IF EXISTS etfs;

-- Drop old ETF table if exists (from V19 migration)
DROP TABLE IF EXISTS etf;

-- Update feature_config description to remove mutual fund and ETF mentions
UPDATE feature_config 
SET description = 'Portfolio tracking for stocks, bonds, gold, and real estate'
WHERE feature_key = 'INVESTMENTS_MODULE';

-- Remove MUTUAL_FUNDS and ETF from any feature flag lists if they exist as separate entries
-- (This is a safeguard in case they were added as separate features)
DELETE FROM feature_config WHERE feature_key IN ('MUTUAL_FUNDS', 'ETF');

SET FOREIGN_KEY_CHECKS = 1;