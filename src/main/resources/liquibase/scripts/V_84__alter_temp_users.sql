-- liquibase formatted sql

-- changeset fifimova:84
ALTER TABLE temporary_users
    ADD COLUMN previous_email TEXT;