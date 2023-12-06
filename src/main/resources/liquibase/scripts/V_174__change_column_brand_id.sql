-- liquibase formatted sql

-- changeset x3imal:174
ALTER TABLE car_brand ALTER COLUMN id TYPE BIGINT;

ALTER TABLE cars ALTER COLUMN brand_id  TYPE BIGINT;

ALTER TABLE cars ALTER COLUMN brand_id DROP DEFAULT;

DROP SEQUENCE IF EXISTS cars_brand_id_seq;

ALTER TABLE cars ALTER COLUMN brand_id SET DEFAULT nextval('car_brand_id_seq');

