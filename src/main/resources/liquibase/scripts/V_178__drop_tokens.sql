-- liquibase formatted sql

-- changeset fifimova:178
ALTER TABLE tokens
    DROP CONSTRAINT token_user_fk;

DROP TABLE tokens;