-- liquibase formatted sql

-- changeset fifimova:11
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- changeset fifimova:11.1
ALTER TABLE users
    ALTER COLUMN full_name SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN email SET NOT NULL;

ALTER TABLE users
    ADD CONSTRAINT email_unique UNIQUE (email);

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN id DROP DEFAULT;

ALTER TABLE users
    ALTER COLUMN id TYPE uuid USING (uuid_generate_v4());

ALTER TABLE users
    ALTER COLUMN id SET DEFAULT gen_random_uuid();
