-- liquibase formatted sql

-- changeset x3imal:17
CREATE TABLE car_brand
(
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(255) NOT NULL
);

-- changeset x3imal:17.1
CREATE TABLE car_model
(
    id       BIGSERIAL PRIMARY KEY,
    brand_id BIGINT REFERENCES car_brand (id),
    model    VARCHAR(255) NOT NULL
);

-- changeset x3imal:17.2
ALTER TABLE cars
    DROP COLUMN brand_model;

-- changeset x3imal:17.3
ALTER TABLE cars
    ADD COLUMN brand_id BIGINT REFERENCES car_brand (id),
    ADD COLUMN model_id BIGINT REFERENCES car_model (id);

-- changeset x3imal:17.4
ALTER TABLE cars
    DROP COLUMN brand_id,
    ADD COLUMN brand_id BIGSERIAL REFERENCES car_brand (id);

