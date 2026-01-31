-- V27: Add notes field to expenses and incomes tables for Sprint 5 (Export & Reports)

-- Add notes column to expenses table
ALTER TABLE expenses
ADD COLUMN notes VARCHAR(500) NULL
COMMENT 'Additional notes or comments about the expense';

-- Add notes column to incomes table
ALTER TABLE incomes
ADD COLUMN notes VARCHAR(500) NULL
COMMENT 'Additional notes or comments about the income';
