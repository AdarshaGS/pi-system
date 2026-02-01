-- Add Stock API External Services Configuration
-- This migration adds Alpha Vantage and Indian API configuration for real stock prices

-- Insert Alpha Vantage service
INSERT IGNORE INTO external_services (id, service_name) 
VALUES (2, 'ALPHA_VANTAGE');

-- Insert Alpha Vantage properties
INSERT IGNORE INTO external_service_properties (id, name, value, external_service_id) 
VALUES 
  (10, 'base-url', 'https://www.alphavantage.co/query', 
   (SELECT id FROM external_services WHERE service_name = 'ALPHA_VANTAGE')),
  (11, 'api-key', 'demo', 
   (SELECT id FROM external_services WHERE service_name = 'ALPHA_VANTAGE')),
  (12, 'rate-limit-per-minute', '5', 
   (SELECT id FROM external_services WHERE service_name = 'ALPHA_VANTAGE'));

-- Insert Indian Stock API service
INSERT IGNORE INTO external_services (id, service_name) 
VALUES (3, 'INDIAN_STOCK_API');

-- Insert Indian API properties
INSERT IGNORE INTO external_service_properties (id, name, value, external_service_id) 
VALUES 
  (13, 'base-url', 'https://api.indianstocks.com/v1/quote', 
   (SELECT id FROM external_services WHERE service_name = 'INDIAN_STOCK_API')),
  (14, 'api-key', 'your-indian-api-key-here', 
   (SELECT id FROM external_services WHERE service_name = 'INDIAN_STOCK_API')),
  (15, 'enabled', 'true', 
   (SELECT id FROM external_services WHERE service_name = 'INDIAN_STOCK_API'));

-- Note: Update these values with actual API keys before deployment
-- Alpha Vantage: Get free key from https://www.alphavantage.co/support/#api-key
-- Indian API: Replace with your actual Indian stock market API
