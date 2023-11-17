-- liquibase formatted sql

-- changeset fifimova:95
ALTER TABLE users
    DROP COLUMN date_of_birth;

ALTER TABLE temporary_users
    DROP COLUMN date_of_birth;