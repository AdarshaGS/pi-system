CREATE TABLE `consent_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `user_consent` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `consent_id` bigint NOT NULL,
  `agreed` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `consent_id` (`consent_id`),
  CONSTRAINT `user_consent_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `user_consent_ibfk_2` FOREIGN KEY (`consent_id`) REFERENCES `consent_template` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);