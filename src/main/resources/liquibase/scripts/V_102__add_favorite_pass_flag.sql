-- liquibase formatted sql

-- changeset Ldv236:102
ALTER TABLE passes
    ADD COLUMN favorite BOOLEAN DEFAULT FALSE;
