-- liquibase formatted sql

-- changeset fifimova:19
ALTER TABLE users
    ADD COLUMN role TEXT;