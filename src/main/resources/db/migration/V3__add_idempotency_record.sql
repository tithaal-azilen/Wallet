CREATE TABLE IF NOT EXISTS idempotency_record (
    idempotency_key VARCHAR(255) PRIMARY KEY,
    request_path VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    response_body TEXT,
    response_status_code INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE
);
