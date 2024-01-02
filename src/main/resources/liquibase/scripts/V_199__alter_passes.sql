-- liquibase formatted sql

-- changeset Ldv236:199
ALTER TABLE passes
    RENAME COLUMN type_time TO time_type;