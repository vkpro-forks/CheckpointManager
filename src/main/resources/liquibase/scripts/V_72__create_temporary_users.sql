-- liquibase formatted sql

-- changeset fifimova:72
CREATE TABLE temporary_users
(
    id             UUID DEFAULT gen_random_uuid(),
    full_name      TEXT,
    date_of_birth  DATE,
    main_number    TEXT,
    email          TEXT,
    password       TEXT,
    verified_token TEXT,

    CONSTRAINT temporary_user_pk PRIMARY KEY (id)
);

-- changeset fifimova:72.1
ALTER TABLE temporary_users
    ADD COLUMN added_at TIMESTAMP;