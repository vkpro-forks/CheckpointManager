-- liquibase formatted sql

-- changeset fifimova:25
ALTER TABLE users
    ADD COLUMN main_number TEXT;

-- changeset fifimova:25.1
ALTER TABLE users
    ADD COLUMN added_at timestamp;

-- changeset fifimova:25.2
ALTER TABLE phones
    DROP CONSTRAINT phone_fk_user;

ALTER TABLE phones
    ADD CONSTRAINT phone_fk_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE;