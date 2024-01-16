-- liquibase formatted sql

-- changeset Ldv236:255
ALTER TABLE territories
    ADD COLUMN city     TEXT,
    ADD COLUMN address  TEXT;