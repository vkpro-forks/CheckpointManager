-- liquibase formatted sql

-- changeset fifimova:22
CREATE TABLE phones
(
    id      UUID DEFAULT gen_random_uuid(),
    number  TEXT NOT NULL,
    type    TEXT,
    user_id UUID NOT NULL,
    note    TEXT,

    CONSTRAINT phone_pk PRIMARY KEY (id)
);

-- changeset fifimova:22.1
ALTER TABLE phones
    ALTER COLUMN type SET DEFAULT 'MOBILE';

--changeset fifimova:22.2
ALTER TABLE phones
    ALTER COLUMN type DROP DEFAULT;

-- changeset fifimova:22.3
ALTER TABLE phones
    ADD CONSTRAINT phone_fk_user FOREIGN KEY (user_id) REFERENCES users (id);