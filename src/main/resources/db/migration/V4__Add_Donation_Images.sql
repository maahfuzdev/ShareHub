ALTER TABLE donations
    ADD COLUMN has_image BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE donation_images (
    donation_id BIGINT PRIMARY KEY REFERENCES donations(id) ON DELETE CASCADE,
    image_data BYTEA NOT NULL,
    content_type VARCHAR(30) NOT NULL
);
