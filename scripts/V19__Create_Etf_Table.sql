CREATE TABLE etfs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entity_type INT,
    symbol VARCHAR(255) UNIQUE,
    name VARCHAR(255) NOT NULL,
    price DOUBLE,
    description TEXT,
    total_expense_ratio DOUBLE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
