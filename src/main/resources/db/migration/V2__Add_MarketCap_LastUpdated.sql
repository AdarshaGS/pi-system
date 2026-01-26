-- Add Market Cap and Last Updated columns to stocks table
ALTER TABLE `stocks`
ADD COLUMN `market_cap` bigint DEFAULT NULL AFTER `sector_id`;
