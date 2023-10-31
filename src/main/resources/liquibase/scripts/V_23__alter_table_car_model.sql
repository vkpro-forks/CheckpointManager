-- liquibase formatted sql

-- changeset x3imal:23
ALTER TABLE car_model
    ADD CONSTRAINT model_unique UNIQUE (model);