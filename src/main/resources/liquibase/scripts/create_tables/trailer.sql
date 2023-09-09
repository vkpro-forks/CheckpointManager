-- liquibase formatted sql

-- changeset x3imal:37
CREATE TABLE trailer (
                         id BIGSERIAL PRIMARY KEY,
                         license_plate VARCHAR(10) NOT NULL,
                         color VARCHAR(255) NULL
);

