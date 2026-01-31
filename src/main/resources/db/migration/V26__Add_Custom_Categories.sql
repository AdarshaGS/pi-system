-- Add custom category support to budgets and expenses tables
-- This migration allows users to create their own budget categories

-- Add custom_category_name column to budgets table
ALTER TABLE budgets 
ADD COLUMN custom_category_name VARCHAR(50);

-- Make category column nullable (to support custom categories)
ALTER TABLE budgets 
MODIFY COLUMN category VARCHAR(50) NULL;

-- Add custom_category_name column to expenses table
ALTER TABLE expenses 
ADD COLUMN custom_category_name VARCHAR(50);

-- Make category column nullable (to support custom categories)
ALTER TABLE expenses 
MODIFY COLUMN category VARCHAR(50) NULL;

-- Create custom_categories table
CREATE TABLE IF NOT EXISTS custom_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    color VARCHAR(20),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    CONSTRAINT fk_custom_category_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT uq_user_category UNIQUE (user_id, category_name)
);

-- Create index for faster lookups
CREATE INDEX idx_custom_categories_user_active ON custom_categories(user_id, is_active);

-- Add check constraint: either category or custom_category_name must be set (but not both)
-- Note: MySQL doesn't support complex check constraints, so this will be enforced at application level
