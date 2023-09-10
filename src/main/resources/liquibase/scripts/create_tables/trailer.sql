-- liquibase formatted sql

-- changeset x3imal:37
CREATE TABLE trailer (
                         id BIGSERIAL PRIMARY KEY,
                         license_plate VARCHAR(10) NOT NULL,
                         color VARCHAR(255) NULL
);


-- changeset x3imal:37.4
ALTER TABLE trailer
    ALTER COLUMN id DROP DEFAULT;

ALTER TABLE trailer
ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());

ALTER TABLE trailer
    ALTER COLUMN id SET DEFAULT gen_random_uuid();