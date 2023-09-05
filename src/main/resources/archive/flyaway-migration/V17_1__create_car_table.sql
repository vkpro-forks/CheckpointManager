ALTER TABLE cars
DROP COLUMN brand_model;

ALTER TABLE cars
DROP COLUMN brand_id,
    ADD COLUMN brand_id BIGSERIAL REFERENCES car_brand(id);
