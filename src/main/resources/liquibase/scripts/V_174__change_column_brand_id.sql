-- liquibase formatted sql

-- changeset x3imal:174

ALTER TABLE cars ALTER COLUMN brand_id  TYPE BIGINT;

ALTER TABLE cars ALTER COLUMN brand_id DROP DEFAULT;

DROP SEQUENCE IF EXISTS cars_brand_id_seq;


