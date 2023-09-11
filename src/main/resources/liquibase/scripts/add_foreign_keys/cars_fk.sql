-- liquibase formatted sql

-- changeset x3imal:17
ALTER TABLE cars
    ADD COLUMN brand_id BIGINT REFERENCES car_brand (id),
    ADD COLUMN model_id BIGINT REFERENCES car_model (id);

-- changeset x3imal:17.1
ALTER TABLE cars
    DROP COLUMN brand_id,
    ADD COLUMN brand_id BIGSERIAL REFERENCES car_brand (id);


