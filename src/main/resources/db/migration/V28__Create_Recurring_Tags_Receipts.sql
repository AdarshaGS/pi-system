-- V28: Sprint 6 - Recurring Transactions, Tags, and Receipts

-- Create recurring_templates table
CREATE TABLE recurring_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL COMMENT 'EXPENSE or INCOME',
    name VARCHAR(100) NOT NULL COMMENT 'Template name',
    category VARCHAR(50) NULL COMMENT 'Expense category',
    custom_category_name VARCHAR(50) NULL COMMENT 'Custom category name',
    source VARCHAR(100) NULL COMMENT 'Income source',
    amount DECIMAL(15, 2) NOT NULL,
    pattern VARCHAR(20) NOT NULL COMMENT 'DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY',
    start_date DATE NOT NULL,
    end_date DATE NULL COMMENT 'NULL means indefinite',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_generated DATE NULL COMMENT 'Last date transaction was generated',
    description VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_active (user_id, is_active),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Recurring transaction templates';

-- Create tags table
CREATE TABLE tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    color VARCHAR(7) NULL DEFAULT '#6366f1' COMMENT 'Hex color code',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_tag (user_id, name),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Tags for categorizing transactions';

-- Create expense_tags junction table (many-to-many)
CREATE TABLE expense_tags (
    expense_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (expense_id, tag_id),
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE,
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many relationship between expenses and tags';

-- Create receipts table
CREATE TABLE receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL COMMENT 'Stored filename',
    original_file_name VARCHAR(255) NOT NULL COMMENT 'Original upload filename',
    file_url VARCHAR(500) NOT NULL COMMENT 'File path or URL',
    content_type VARCHAR(100) NULL COMMENT 'MIME type',
    file_size BIGINT NULL COMMENT 'Size in bytes',
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    INDEX idx_expense_id (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Receipt attachments for expenses';
