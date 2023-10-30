-- liquibase formatted sql

-- changeset fifimova:21
ALTER TABLE users
    ALTER COLUMN is_blocked SET DEFAULT false;