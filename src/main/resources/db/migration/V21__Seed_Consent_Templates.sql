ALTER TABLE `consent_template` ADD COLUMN `entity_type` INT NOT NULL DEFAULT 7;

INSERT IGNORE INTO `consent_template` (`message`, `entity_type`) VALUES
('One-time access for account verification and identity validation', 7),
('Comprehensive data access for credit scoring and loan underwriting', 10),
('Periodic access for personal finance management and expense analysis', 6),
('Ongoing access for wealth management, portfolio tracking, and investment advice', 2),
('Specific access for automated tax filing and financial audits', 24);
