CREATE TABLE resource_requests (
    id BIGSERIAL PRIMARY KEY,
    donation_id BIGINT NOT NULL REFERENCES donations(id),
    recipient_id BIGINT NOT NULL REFERENCES users(id),
    message VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_resource_requests_recipient_created_at
    ON resource_requests(recipient_id, created_at DESC);

CREATE INDEX idx_resource_requests_donation_status
    ON resource_requests(donation_id, status);
