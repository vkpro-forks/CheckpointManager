-- liquibase formatted sql

-- changeset x3imal:17
CREATE TABLE car_brand
(
    id    BIGSERIAL PRIMARY KEY,
    brand VARCHAR(255) NOT NULL
);

CREATE TABLE car_model
(
    id       BIGSERIAL PRIMARY KEY,
    brand_id BIGINT REFERENCES car_brand (id),
    model    VARCHAR(255) NOT NULL
);


-- changeset x3imal:23
ALTER TABLE car_model
    ADD CONSTRAINT model_unique UNIQUE (model);
