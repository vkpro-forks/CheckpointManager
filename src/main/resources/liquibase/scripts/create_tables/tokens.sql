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
