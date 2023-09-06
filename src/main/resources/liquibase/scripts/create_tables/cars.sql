-- liquibase formatted sql

-- changeset x3imal:13
CREATE TABLE cars
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    license_plate VARCHAR(255) NOT NULL,
    brand_model   VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    color         VARCHAR(255) NOT NULL,
    year          INTEGER
);

-- changeset x3imal:17.1
ALTER TABLE cars
    DROP COLUMN brand_model;

