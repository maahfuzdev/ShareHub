CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    category VARCHAR(20) NOT NULL,
    quantity VARCHAR(80) NOT NULL,
    location VARCHAR(160) NOT NULL,
    available_until DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    donor_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donations_status_created_at ON donations(status, created_at DESC);
CREATE INDEX idx_donations_donor_id ON donations(donor_id);
