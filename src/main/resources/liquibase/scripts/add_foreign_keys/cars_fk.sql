-- liquibase formatted sql

-- changeset x3imal:17
ALTER TABLE cars
    ADD COLUMN brand_id BIGINT REFERENCES car_brand (id),
    ADD COLUMN model_id BIGINT REFERENCES car_model (id);

-- changeset x3imal:17.1
ALTER TABLE cars
    DROP COLUMN brand_id,
    ADD COLUMN brand_id BIGSERIAL REFERENCES car_brand (id);

-- changeset x3imal:37.1
ALTER TABLE cars
    ADD COLUMN trailer_id BIGINT NULL,
ADD CONSTRAINT fk_cars_trailer FOREIGN KEY (trailer_id) REFERENCES trailer(id);

-- changeset x3imal:37.2
ALTER TABLE cars
DROP CONSTRAINT fk_cars_trailer;

-- changeset x3imal:37.3
ALTER TABLE cars
DROP COLUMN trailer_id;