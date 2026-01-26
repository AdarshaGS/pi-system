CREATE TABLE third_party_request_audit (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(100) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    method VARCHAR(10) NOT NULL,
    request_headers TEXT,
    request_body LONGTEXT,
    response_status INT,
    response_headers TEXT,
    response_body LONGTEXT,
    time_taken_ms BIGINT,
    timestamp DATETIME NOT NULL,
    exception_message TEXT
);



CREATE INDEX idx_tp_audit_provider ON third_party_request_audit(`provider_name`);
CREATE INDEX idx_tp_audit_timestamp ON third_party_request_audit(`timestamp`);
