-- RBAC Implementation
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Seed Roles
INSERT IGNORE INTO `roles` (`name`) VALUES ('ROLE_USER_READ_ONLY');
INSERT IGNORE INTO `roles` (`name`) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO `roles` (`name`) VALUES ('ROLE_SUPER_ADMIN');

-- Assign ROLE_USER_READ_ONLY to all existing users
INSERT IGNORE INTO `user_roles` (user_id, role_id)
SELECT u.id, r.id FROM `users` u, `roles` r 
WHERE r.name = 'ROLE_USER_READ_ONLY';
