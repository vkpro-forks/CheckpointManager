-- liquibase formatted sql

-- changeset Ldv236:54
ALTER TABLE passes
    ADD COLUMN name text;