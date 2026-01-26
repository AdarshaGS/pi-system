ALTER TABLE `aa_consents` ADD COLUMN `entity_type` INT NOT NULL DEFAULT 0;

ALTER TABLE `stocks`
ADD INDEX `fk_sector_id` (`sector_id`) USING BTREE;