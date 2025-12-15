CREATE TABLE request_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255),
    method VARCHAR(10) NOT NULL,
    uri VARCHAR(2048) NOT NULL,
    query_params VARCHAR(2048),
    status_code INT NOT NULL,
    time_taken_ms BIGINT NOT NULL,
    timestamp DATETIME NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512)
);

CREATE INDEX idx_request_audit_user_id ON request_audit(user_id);
CREATE INDEX idx_request_audit_timestamp ON request_audit(timestamp);
