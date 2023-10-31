-- liquibase formatted sql

-- changeset Ldv236:9
ALTER TABLE checkpoints
    ADD COLUMN added_at timestamp;