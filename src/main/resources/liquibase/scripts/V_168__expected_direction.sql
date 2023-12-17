-- liquibase formatted sql

-- changeset Ldv236:168
ALTER TABLE passes
    ADD COLUMN expected_direction TEXT;