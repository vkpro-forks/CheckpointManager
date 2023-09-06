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

-- changeset fifimova:31
ALTER TABLE phones
    ALTER COLUMN type DROP DEFAULT;

