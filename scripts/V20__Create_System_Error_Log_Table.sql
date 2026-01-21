CREATE TABLE system_error_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    request_id VARCHAR(255),
    status INT,
    error_code VARCHAR(255),
    message TEXT,
    path VARCHAR(500),
    stack_trace TEXT,
    method VARCHAR(20)
);

CREATE INDEX idx_error_logs_request_id ON system_error_logs(request_id);
CREATE INDEX idx_error_logs_timestamp ON system_error_logs(timestamp);
