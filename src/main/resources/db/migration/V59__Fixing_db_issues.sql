ALTER TABLE `pi_system`.`bank_accounts`
ADD COLUMN `balance` double NOT NULL;

ALTER TABLE `bank_accounts`
CHANGE `user_id` `user_id` bigint NULL;

ALTER TABLE `etfs`
ADD COLUMN `price` double NULL AFTER `description`;

ALTER TABLE `etfs`
ADD COLUMN `total_expense_ratio` double NOT NULL AFTER `description`;


ALTER TABLE `goal_milestones`
CHANGE `iid` `id` bigint NOT NULL auto_increment;

ALTER TABLE `loan_payments`
CHANGE `created_at` `created_at` date NOT NULL;



CREATE TABLE IF NOT EXISTS user_notifications (
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	user_id BIGINT NOT NULL,
	title VARCHAR(255) NOT NULL,
	message VARCHAR(1000),
	TYPE VARCHAR(50) NOT NULL,
	is_read BOOLEAN NOT NULL DEFAULT FALSE,
	created_at DATETIME NOT NULL,
	read_at DATETIME,
	alert_rule_id BIGINT,
	channel VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS notification_metadata (
	notification_id BIGINT NOT NULL,
	meta_key VARCHAR(255) NOT NULL,
	meta_value VARCHAR(255),
	PRIMARY KEY (notification_id, meta_key),
CONSTRAINT fk_notification_metadata_notification FOREIGN KEY (notification_id) REFERENCES user_notifications (id) ON DELETE CASCADE
);

ALTER TABLE `transaction_requests`
ADD COLUMN `remarks` varchar(255) NOT NULL AFTER `payer_upi_id`;


ALTER TABLE `upi_ids`
CHANGE `user_id` `user_id` bigint NULL;


ALTER TABLE `upi_pins`
CHANGE `user_id` `user_id` bigint NULL;


CREATE TABLE upi_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payer_vpa VARCHAR(255),
    payee_vpa VARCHAR(255),
    amount DOUBLE NOT NULL,
    remarks VARCHAR(255),
    transaction_id VARCHAR(255),
    status VARCHAR(50),
    created_at DATETIME
);