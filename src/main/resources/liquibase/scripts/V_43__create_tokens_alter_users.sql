-- liquibase formatted sql

-- changeset fifimova:43
CREATE TABLE tokens
(
    id         UUID DEFAULT gen_random_uuid(),
    token      TEXT UNIQUE NOT NULL,
    token_type TEXT        NOT NULL,
    revoked    BOOLEAN,
    expired    BOOLEAN,
    user_id    UUID        NOT NULL,

    CONSTRAINT token_pk PRIMARY KEY (id)

);

-- changeset fifimova:43.1
ALTER TABLE users
    ADD CONSTRAINT number_unique UNIQUE (main_number);

-- changeset fifimova:43.2
ALTER TABLE tokens
    ADD CONSTRAINT token_user_fk
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE;

