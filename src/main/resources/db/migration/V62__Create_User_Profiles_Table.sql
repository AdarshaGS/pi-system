-- Create user_profiles table for storing financial and personal profile data
CREATE TABLE `user_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `date_of_birth` date DEFAULT NULL,
  `dependents` int DEFAULT 0,
  `city` varchar(100) DEFAULT NULL,
  `city_tier` varchar(20) DEFAULT 'METRO',
  `annual_income` decimal(19,2) DEFAULT NULL,
  `monthly_income` decimal(19,2) DEFAULT NULL,
  `employment_type` varchar(30) DEFAULT 'SALARIED',
  `emergency_fund_months` int DEFAULT 6,
  `risk_tolerance` varchar(20) DEFAULT 'MODERATE',
  `retirement_age` int DEFAULT 60,
  `life_expectancy` int DEFAULT 80,
  `is_profile_complete` boolean DEFAULT FALSE,
  `created_at` timestamp DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_user_id` (`user_id`),
  CONSTRAINT `FK_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
