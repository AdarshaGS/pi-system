CREATE TABLE incomes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    source VARCHAR(255) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    date DATE NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    is_stable BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_incomes_user FOREIGN KEY (user_id) REFERENCES users(id)
);
