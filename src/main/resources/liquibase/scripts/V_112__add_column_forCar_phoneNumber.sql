-- liquibase formatted sql

-- changeset x3imal:112

ALTER TABLE cars
    ADD COLUMN car_phone VARCHAR(40);
