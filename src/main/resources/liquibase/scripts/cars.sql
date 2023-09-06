-- liquibase formatted sql

-- changeset x3imal:13 context:ignore
CREATE TABLE cars
(
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    license_plate VARCHAR(255) NOT NULL,
    brand_model   VARCHAR(255) NOT NULL,
    type          VARCHAR(255) NOT NULL,
    color         VARCHAR(255) NOT NULL,
    year          INTEGER
);

-- changeset x3imal:17 context:ignore
ALTER TABLE cars
    ADD COLUMN brand_id BIGINT REFERENCES car_brand (id),
    ADD COLUMN model_id BIGINT REFERENCES car_model (id);

-- changeset x3imal:17.1 context:ignore
ALTER TABLE cars
DROP
COLUMN brand_model;

ALTER TABLE cars
DROP
COLUMN brand_id,
    ADD COLUMN brand_id BIGSERIAL REFERENCES car_brand (id);
