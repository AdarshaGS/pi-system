-- Initial Schema
CREATE TABLE `sectors` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `code` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `stocks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `symbol` varchar(255) DEFAULT NULL,
  `company_name` varchar(255) NOT NULL,
  `price` double DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sector_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_symbol` (`symbol`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `portfolio_holdings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `stock_symbol` varchar(255) NOT NULL,
  `quantity` int NOT NULL,
  `purchase_price` decimal(19,4) NOT NULL,
  `current_price` decimal(19,4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recommendations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `threshold_percentage` double DEFAULT NULL,
  `recommendation_message` varchar(2000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed Sectors
INSERT IGNORE INTO `sectors` (`id`, `name`, `code`) VALUES
(1, 'Energy', 'ENE'),
(2, 'Materials', 'MAT'),
(3, 'Industrials', 'IND'),
(4, 'Consumer Discretionary', 'CND'),
(5, 'Consumer Staples', 'CNS'),
(6, 'Health Care', 'HC'),
(7, 'Financials', 'FIN'),
(8, 'Information Technology', 'IT'),
(9, 'Communication Services', 'COM'),
(10, 'Utilities', 'UTL'),
(11, 'Real Estate', 'RE'),
(12, 'Others', 'OTH');
